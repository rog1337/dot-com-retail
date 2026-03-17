package com.dotcom.retail.cart

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.domain.cart.Cart
import com.dotcom.retail.domain.cart.CartItem
import com.dotcom.retail.domain.cart.CartRepository
import com.dotcom.retail.domain.cart.CartService
import com.dotcom.retail.domain.cart.dto.ItemUpdateRequest
import com.dotcom.retail.domain.cart.dto.CartUpdateRequest
import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.order.Order
import com.dotcom.retail.domain.order.OrderRepository
import com.dotcom.retail.domain.order.OrderStatus
import com.dotcom.retail.domain.order.ShippingType
import com.dotcom.retail.domain.payment.PaymentService
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserService
import com.stripe.exception.InvalidRequestException
import com.stripe.model.PaymentIntent
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class CartServiceTest {

    private val cartRepository = mockk<CartRepository>()
    private val userService = mockk<UserService>()
    private val productService = mockk<ProductService>()
    private val paymentService = mockk<PaymentService>()
    private val orderRepository = mockk<OrderRepository>()

    private val cartService = CartService(
        cartRepository = cartRepository,
        userService = userService,
        productService = productService,
        paymentService = paymentService,
        orderRepository = orderRepository,
    )

    private lateinit var user: User
    private lateinit var userId: UUID
    private lateinit var product: Product

    @BeforeEach
    fun setup() {
        userId = UUID.randomUUID()
        user = User(id = userId, email = "test@test.com", displayName = "Test User")
        product = product(id = 1, price = BigDecimal("100.00"), stock = 10)

        every { cartRepository.save(any()) } answers { firstArg() }
    }

    private fun product(id: Long, price: BigDecimal, stock: Int) =
        Product(id = id, name = "Product $id", price = price, stock = stock, sku = "testSku")

    private fun cartWithItems(vararg items: Pair<Product, Int>): Cart {
        val cart = Cart(
            user = user,
            shippingCost = CartService.SHIPPING_STANDARD_PRICE,
            shippingType = ShippingType.STANDARD,
        )
        items.forEach { (product, qty) ->
            cart.addItem(CartItem(product = product, quantity = qty, priceSnapshot = product.price))
        }
        return cart
    }

    @Nested
    inner class TotalCalculations {

        @Test
        fun `subtotal sums all item line totals`() {
            val cart = cartWithItems(product(1, BigDecimal("100.00"), 10) to 2)
            assertEquals(BigDecimal("200.00"), cart.getSubTotalPrice())
        }

        @Test
        fun `total includes shipping cost`() {
            val cart = cartWithItems(product(1, BigDecimal("100.00"), 10) to 2)
            assertEquals(BigDecimal("205.00"), cart.getTotalPrice())
        }

        @Test
        fun `total with multiple items sums correctly`() {
            val p1 = product(1, BigDecimal("100.00"), 10)
            val p2 = product(2, BigDecimal("115.50"), 10)
            val cart = cartWithItems(p1 to 2, p2 to 1)
            // (100*2) + (115.50*1) + 5 shipping = 320.50
            assertEquals(BigDecimal("320.50"), cart.getTotalPrice())
        }

        @Test
        fun `total with no items is just shipping`() {
            val cart = Cart(shippingCost = CartService.SHIPPING_STANDARD_PRICE)
            assertEquals(CartService.SHIPPING_STANDARD_PRICE, cart.getTotalPrice())
        }

        @Test
        fun `total with null shipping cost treats it as zero`() {
            val cart = Cart(shippingCost = null)
            cart.addItem(CartItem(product = product, quantity = 1, priceSnapshot = BigDecimal("100.00")))
            assertEquals(BigDecimal("100.00"), cart.getTotalPrice())
        }

        @Test
        fun `standard shipping cost is 5`() {
            assertEquals(BigDecimal(5), cartService.calculateShippingCost(ShippingType.STANDARD))
        }

        @Test
        fun `express shipping cost is 15`() {
            assertEquals(BigDecimal(15), cartService.calculateShippingCost(ShippingType.EXPRESS))
        }

        @Test
        fun `quantity totals sum all item quantities`() {
            val p1 = product(1, BigDecimal("100.00"), 10)
            val p2 = product(2, BigDecimal("50.00"), 10)
            val cart = cartWithItems(p1 to 3, p2 to 2)
            assertEquals(5, cart.getTotalQuantity())
        }
    }

    @Nested
    inner class StockValidation {

        @Test
        fun `checkStock passes when all items have sufficient stock`() {
            val cart = cartWithItems(product(1, BigDecimal("100.00"), stock = 5) to 3)
            assertDoesNotThrow { cartService.checkStock(cart) }
        }

        @Test
        fun `checkStock throws when item quantity exceeds stock`() {
            val cart = cartWithItems(product(1, BigDecimal("100.00"), stock = 2) to 5)
            assertThrows<AppException> { cartService.checkStock(cart) }
        }

        @Test
        fun `checkStock throws when quantity is one over stock`() {
            val cart = cartWithItems(product(1, BigDecimal("100.00"), stock = 3) to 4)
            assertThrows<AppException> { cartService.checkStock(cart) }
        }

        @Test
        fun `checkStock passes at exact stock boundary`() {
            val cart = cartWithItems(product(1, BigDecimal("100.00"), stock = 3) to 3)
            assertDoesNotThrow { cartService.checkStock(cart) }
        }

        @Test
        fun `update throws insufficient stock when quantity exceeds stock`() {
            val insufficientProduct = product(id = 1, price = BigDecimal("100.00"), stock = 2)
            every { cartRepository.findByUserId(userId) } returns Cart(user = user)
            every { productService.getAllById(any()) } returns setOf(insufficientProduct)

            val request = CartUpdateRequest(
                items = setOf(ItemUpdateRequest(productId = 1, quantity = 5)),
                shippingType = null
            )

            assertThrows<AppException> { cartService.update(userId, null, request) }
        }
    }

    @Nested
    inner class CartUpdate {

        @Test
        fun `update adds new item to cart`() {
            val cart = Cart(user = user)
            every { cartRepository.findByUserId(userId) } returns cart
            every { productService.getAllById(any()) } returns setOf(product)

            cartService.update(
                userId, null, CartUpdateRequest(
                    items = setOf(ItemUpdateRequest(productId = 1, quantity = 2)),
                    shippingType = null
                )
            )

            assertEquals(1, cart.items.size)
            assertEquals(2, cart.items.first().quantity)
        }

        @Test
        fun `update changes quantity of existing item`() {
            val cart = Cart(user = user)
            cart.addItem(CartItem(product = product, quantity = 1, priceSnapshot = product.price))
            every { cartRepository.findByUserId(userId) } returns cart
            every { productService.getAllById(any()) } returns setOf(product)

            cartService.update(
                userId, null, CartUpdateRequest(
                    items = setOf(ItemUpdateRequest(productId = 1, quantity = 5)),
                    shippingType = null
                )
            )

            assertEquals(5, cart.items.first().quantity)
        }

        @Test
        fun `update removes items not in request`() {
            val p2 = product(id = 2, price = BigDecimal("50.00"), stock = 10)
            val cart = Cart(user = user)
            cart.addItem(CartItem(product = product, quantity = 1, priceSnapshot = product.price))
            cart.addItem(CartItem(product = p2, quantity = 1, priceSnapshot = p2.price))
            every { cartRepository.findByUserId(userId) } returns cart
            every { productService.getAllById(any()) } returns setOf(product)

            cartService.update(
                userId, null, CartUpdateRequest(
                    items = setOf(ItemUpdateRequest(productId = 1, quantity = 1)),
                    shippingType = null
                )
            )

            assertEquals(1, cart.items.size)
            assertEquals(product.id, cart.items.first().product.id)
        }

        @Test
        fun `update with empty items clears cart`() {
            val cart = Cart(user = user)
            cart.addItem(CartItem(product = product, quantity = 2, priceSnapshot = product.price))
            every { cartRepository.findByUserId(userId) } returns cart

            cartService.update(userId, null, CartUpdateRequest(items = emptySet(), shippingType = null))

            assertTrue(cart.items.isEmpty())
        }

        @Test
        fun `update with null items clears cart`() {
            val cart = Cart(user = user)
            cart.addItem(CartItem(product = product, quantity = 1, priceSnapshot = product.price))
            every { cartRepository.findByUserId(userId) } returns cart

            cartService.update(userId, null, CartUpdateRequest(items = null, shippingType = null))

            assertTrue(cart.items.isEmpty())
        }

        @Test
        fun `update sets shipping type and recalculates cost`() {
            val cart = Cart(user = user)
            cart.addItem(CartItem(product = product, quantity = 1, priceSnapshot = product.price))
            every { cartRepository.findByUserId(userId) } returns cart
            every { productService.getAllById(any()) } returns setOf(product)

            cartService.update(
                userId, null, CartUpdateRequest(
                    items = setOf(ItemUpdateRequest(productId = 1, quantity = 1)),
                    shippingType = ShippingType.EXPRESS,
                )
            )

            assertEquals(ShippingType.EXPRESS, cart.shippingType)
            assertEquals(CartService.SHIPPING_EXPRESS_PRICE, cart.shippingCost)
        }

        @Test
        fun `update calls Stripe when intentId exists`() {
            val cart = Cart(user = user, intentId = "pi_test123")
            cart.addItem(CartItem(product = product, quantity = 1, priceSnapshot = product.price))
            every { cartRepository.findByUserId(userId) } returns cart
            every { productService.getAllById(any()) } returns setOf(product)
            every { paymentService.updatePaymentIntent("pi_test123", any()) } returns mockk()

            cartService.update(
                userId, null, CartUpdateRequest(
                    items = setOf(ItemUpdateRequest(productId = 1, quantity = 1)),
                    shippingType = null
                )
            )

            verify(exactly = 1) { paymentService.updatePaymentIntent("pi_test123", any()) }
        }

        @Test
        fun `update clears intentId when Stripe throws InvalidRequestException`() {
            val cart = Cart(user = user, intentId = "pi_expired")
            cart.addItem(CartItem(product = product, quantity = 1, priceSnapshot = product.price))
            every { cartRepository.findByUserId(userId) } returns cart
            every { productService.getAllById(any()) } returns setOf(product)
            every { paymentService.updatePaymentIntent("pi_expired", any()) } throws
                    InvalidRequestException("expired", null, null, null, null, null)

            cartService.update(
                userId, null, CartUpdateRequest(
                    items = setOf(ItemUpdateRequest(productId = 1, quantity = 1)),
                    shippingType = null
                )
            )

            assertNull(cart.intentId)
        }
    }

    @Nested
    inner class Checkout {

        private fun mockIntent(
            id: String = "pi_new",
            secret: String = "secret_new",
            status: String = "requires_payment_method",
        ): PaymentIntent = mockk {
            every { this@mockk.id } returns id
            every { clientSecret } returns secret
            every { this@mockk.status } returns status
        }

        private fun stubLock(cart: Cart) {
            every { cartRepository.lockByUserId(userId) } returns cart
            every { cartRepository.findById(cart.id) } returns Optional.of(cart)
        }

        @Test
        fun `checkout creates new intent when cart has no intentId`() {
            val cart = cartWithItems(product to 1)
            val intent = mockIntent()
            stubLock(cart)
            every { paymentService.createPaymentIntent(any()) } returns intent

            val response = cartService.checkout(userId, null)

            assertEquals("secret_new", response.clientSecret)
            assertEquals("pi_new", cart.intentId)
            verify(exactly = 1) { paymentService.createPaymentIntent(any()) }
        }

        @Test
        fun `checkout reuses intent when order is PENDING_PAYMENT`() {
            val cart = cartWithItems(product to 1).apply { intentId = "pi_existing" }
            val intent = mockIntent(id = "pi_existing", secret = "secret_existing")
            val existingOrder = Order(
                intentId = "pi_existing",
                status = OrderStatus.PENDING_PAYMENT,
                totalAmount = BigDecimal("105.00")
            )

            stubLock(cart)
            every { orderRepository.findByIntentId("pi_existing") } returns existingOrder
            every { paymentService.retrieveIntent("pi_existing") } returns intent

            val response = cartService.checkout(userId, null)

            assertEquals("secret_existing", response.clientSecret)
            verify(exactly = 0) { paymentService.createPaymentIntent(any()) }
        }

        @Test
        fun `checkout reuses intent when no order exists and intent still open`() {
            val cart = cartWithItems(product to 1).apply { intentId = "pi_existing" }
            val intent = mockIntent(id = "pi_existing", secret = "secret_existing")

            stubLock(cart)
            every { orderRepository.findByIntentId("pi_existing") } returns null
            every { paymentService.retrieveIntent("pi_existing") } returns intent

            val response = cartService.checkout(userId, null)

            assertEquals("secret_existing", response.clientSecret)
            verify(exactly = 0) { paymentService.createPaymentIntent(any()) }
        }

        @Test
        fun `checkout creates new intent when previous order is PAID`() {
            val cart = cartWithItems(product to 1).apply { intentId = "pi_old" }
            val paidOrder = Order(intentId = "pi_old", status = OrderStatus.PAID, totalAmount = BigDecimal("105.00"))
            val newIntent = mockIntent(id = "pi_new", secret = "secret_new")

            stubLock(cart)
            every { orderRepository.findByIntentId("pi_old") } returns paidOrder
            every { paymentService.createPaymentIntent(any()) } returns newIntent

            val response = cartService.checkout(userId, null)

            assertEquals("secret_new", response.clientSecret)
            verify(exactly = 1) { paymentService.createPaymentIntent(any()) }
        }

        @Test
        fun `checkout throws when cart is empty`() {
            val emptyCart = Cart(user = user)
            stubLock(emptyCart)

            assertThrows<AppException> { cartService.checkout(userId, null) }
            verify(exactly = 0) { paymentService.createPaymentIntent(any()) }
        }

        @Test
        fun `checkout throws when stock is insufficient`() {
            val cart = cartWithItems(product(id = 1, price = BigDecimal("100.00"), stock = 0) to 1)
            stubLock(cart)

            assertThrows<AppException> { cartService.checkout(userId, null) }
            verify(exactly = 0) { paymentService.createPaymentIntent(any()) }
        }

        @Test
        fun `checkout sets standard shipping on new intent`() {
            val cart = cartWithItems(product to 1)
            val intent = mockIntent()
            stubLock(cart)
            every { paymentService.createPaymentIntent(any()) } returns intent

            cartService.checkout(userId, null)

            assertEquals(ShippingType.STANDARD, cart.shippingType)
            assertEquals(CartService.SHIPPING_STANDARD_PRICE, cart.shippingCost)
        }

        @Test
        fun `checkout passes correct total to Stripe`() {
            val cart = cartWithItems(product(1, BigDecimal("100.00"), 10) to 1)
            val intent = mockIntent()
            stubLock(cart)
            every { paymentService.createPaymentIntent(any()) } returns intent

            cartService.checkout(userId, null)

            // 100 + 5 shipping = 105
            verify { paymentService.createPaymentIntent(BigDecimal("105.00")) }
        }
    }

    // ── Guest cart ─────────────────────────────────────────────────────────

    @Nested
    inner class GuestCart {

        @Test
        fun `getActiveCart creates guest cart when session has no cart`() {
            val sessionId = "session-abc"
            every { cartRepository.findBySessionId(sessionId) } returns null

            cartService.getActiveCart(null, sessionId)

            verify(exactly = 1) { cartRepository.save(any()) }
        }

        @Test
        fun `getActiveCart returns existing cart for session`() {
            val sessionId = "session-abc"
            val existingCart = Cart(sessionId = sessionId)
            every { cartRepository.findBySessionId(sessionId) } returns existingCart

            val cart = cartService.getActiveCart(null, sessionId)

            assertEquals(existingCart, cart)
            verify(exactly = 0) { cartRepository.save(any()) }
        }

        @Test
        fun `getActiveCart creates user cart when none exists`() {
            every { cartRepository.findByUserId(userId) } returns null
            every { userService.getById(userId) } returns user

            cartService.getActiveCart(userId, null)

            verify { cartRepository.save(match { it.user?.id == userId }) }
        }
    }
}