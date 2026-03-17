package com.dotcom.retail.order

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.model.AddressFields
import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.common.service.EncryptionService
import com.dotcom.retail.domain.cart.Cart
import com.dotcom.retail.domain.cart.CartItem
import com.dotcom.retail.domain.cart.CartService
import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.order.Order
import com.dotcom.retail.domain.order.OrderItem
import com.dotcom.retail.domain.order.OrderRepository
import com.dotcom.retail.domain.order.OrderService
import com.dotcom.retail.domain.order.OrderStatus
import com.dotcom.retail.domain.order.ShippingType
import com.dotcom.retail.domain.order.dto.OrderAddress
import com.dotcom.retail.domain.order.dto.SubmitOrderRequest
import com.dotcom.retail.domain.payment.PaymentService
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserService
import com.stripe.model.PaymentIntent
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class OrderServiceTest {

    private val cartService = mockk<CartService>()
    private val userService = mockk<UserService>()
    private val productService = mockk<ProductService>()
    private val orderRepository = mockk<OrderRepository>()
    private val paymentService = mockk<PaymentService>()
    private val encryptionService = mockk<EncryptionService>()

    private val orderService = OrderService(
        cartService = cartService,
        userService = userService,
        productService = productService,
        orderRepository = orderRepository,
        paymentService = paymentService,
        encryptionService = encryptionService,
    )

    private val userId = UUID.randomUUID()
    private val intentId = "pi_test123"

    @BeforeEach
    fun setup() {
        every { orderRepository.save(any()) } answers { firstArg() }
        every { encryptionService.encrypt(any()) } answers { firstArg() }
        every { cartService.checkStock(any()) } just Runs
        every { userService.getById(userId) } returns User(id = userId, email = "test@test.com", displayName = "Test User")
        every { userService.findById(userId) } returns User(id = userId, email = "test@test.com", displayName = "Test User")
        every { userService.save(any()) } returns User(id = userId, email = "test@test.com", displayName = "Test User")
        every { paymentService.updatePaymentIntent(any<PaymentIntent>(), any()) } returns mockk()
    }

    private fun product(id: Long, price: BigDecimal, stock: Int = 10) =
        Product(id = id, name = "Product $id", price = price, stock = stock, sku = "testSku")

    private fun cartWithItems(intentId: String? = this.intentId, vararg items: Pair<Product, Int>): Cart {
        val cart = Cart(
            intentId = intentId,
            shippingType = ShippingType.STANDARD,
            shippingCost = CartService.SHIPPING_STANDARD_PRICE,
        )
        items.forEach { (p, qty) ->
            cart.addItem(CartItem(product = p, quantity = qty, priceSnapshot = p.price))
        }
        return cart
    }

    private fun submitRequest(shippingType: ShippingType = ShippingType.STANDARD) = SubmitOrderRequest(
        name = "Jenny Rosen",
        email = "jenny@example.com",
        phone = "+372123456",
        shippingType = shippingType,
        address = OrderAddress(
            streetLine1 = "Valge 1",
            streetLine2 = null,
            stateOrProvince = null,
            city = "Tallinn",
            postalCode = "11413",
            country = "EE",
        ),
        notes = null,
    )

    private fun mockIntent(status: String = "requires_payment_method"): PaymentIntent = mockk {
        every { id } returns intentId
        every { this@mockk.status } returns status
    }

    private fun pendingOrder(vararg items: Pair<Product, Int>): Order {
        val order = Order(
            intentId = intentId,
            status = OrderStatus.PENDING_PAYMENT,
            totalAmount = BigDecimal("105.00"),
        )
        items.forEach { (p, qty) ->
            order.addItem(OrderItem(order = order, product = p, productName = p.name, quantity = qty, price = p.price))
        }
        return order
    }

    @Nested
    inner class OrderSummaryCalculations {

        @Test
        fun `submitOrder total is item price plus standard shipping`() {
            val cart = cartWithItems(intentId, product(1, BigDecimal("100.00")) to 1)
            val intent = mockIntent()

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(ShippingType.STANDARD) } returns BigDecimal("5.00")
            every { orderRepository.findByIntentId(intentId) } returns null
            every { paymentService.retrieveIntent(intentId) } returns intent

            val order = orderService.submitOrder(userId, null, submitRequest())

            assertEquals(BigDecimal("105.00"), order.totalAmount)
        }

        @Test
        fun `submitOrder total with express shipping is higher`() {
            val cart = cartWithItems(intentId, product(1, BigDecimal("100.00")) to 1)
            val intent = mockIntent()

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(ShippingType.EXPRESS) } returns BigDecimal("15.00")
            every { orderRepository.findByIntentId(intentId) } returns null
            every { paymentService.retrieveIntent(intentId) } returns intent

            val order = orderService.submitOrder(userId, null, submitRequest(ShippingType.EXPRESS))

            assertEquals(BigDecimal("115.00"), order.totalAmount)
        }

        @Test
        fun `submitOrder total accumulates multiple items correctly`() {
            val p1 = product(1, BigDecimal("120.00"))
            val p2 = product(2, BigDecimal("115.50"))
            val cart = cartWithItems(intentId, p1 to 2, p2 to 1)
            val intent = mockIntent()

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(ShippingType.STANDARD) } returns BigDecimal("5.00")
            every { orderRepository.findByIntentId(intentId) } returns null
            every { paymentService.retrieveIntent(intentId) } returns intent

            val order = orderService.submitOrder(userId, null, submitRequest())

            assertEquals(BigDecimal("360.50"), order.totalAmount)
        }

        @Test
        fun `submitOrder total with high quantity is correct`() {
            val cart = cartWithItems(intentId, product(1, BigDecimal("265.00")) to 4)
            val intent = mockIntent()

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(ShippingType.STANDARD) } returns BigDecimal("5.00")
            every { orderRepository.findByIntentId(intentId) } returns null
            every { paymentService.retrieveIntent(intentId) } returns intent

            val order = orderService.submitOrder(userId, null, submitRequest())

            assertEquals(BigDecimal("1065.00"), order.totalAmount)
        }

        @Test
        fun `order item totalAmount is price times quantity`() {
            val item = OrderItem(
                order = mockk(relaxed = true),
                product = product(1, BigDecimal("115.50")),
                productName = "Product 1",
                quantity = 2,
                price = BigDecimal("115.50"),
            )
            assertEquals(BigDecimal("231.00"), item.totalAmount())
        }

        @Test
        fun `submitOrder syncs correct total with Stripe`() {
            val cart = cartWithItems(intentId, product(1, BigDecimal("100.00")) to 1)
            val intent = mockIntent()

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(ShippingType.STANDARD) } returns BigDecimal("5.00")
            every { orderRepository.findByIntentId(intentId) } returns null
            every { paymentService.retrieveIntent(intentId) } returns intent
            every { paymentService.updatePaymentIntent(intent, any()) } returns mockk()

            orderService.submitOrder(userId, null, submitRequest())

            verify { paymentService.updatePaymentIntent(intent, BigDecimal("105.00")) }
        }
    }

    @Nested
    inner class SubmitOrder {

        @Test
        fun `submitOrder creates new order when none exists`() {
            val cart = cartWithItems(intentId, product(1, BigDecimal("100.00")) to 1)
            val intent = mockIntent()

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(any()) } returns BigDecimal("5.00")
            every { orderRepository.findByIntentId(intentId) } returns null
            every { paymentService.retrieveIntent(intentId) } returns intent
            every { paymentService.updatePaymentIntent(intent, any()) } returns mockk()

            val order = orderService.submitOrder(userId, null, submitRequest())

            assertEquals(OrderStatus.PENDING_PAYMENT, order.status)
            assertEquals(1, order.items.size)
            assertNotNull(order.contact)
        }

        @Test
        fun `submitOrder updates existing order on re-submit`() {
            val cart = cartWithItems(intentId, product(1, BigDecimal("100.00")) to 1)
            val intent = mockIntent()
            val existingOrder =
                Order(intentId = intentId, status = OrderStatus.PENDING_PAYMENT, totalAmount = BigDecimal("50.00"))

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(any()) } returns BigDecimal("5.00")
            every { orderRepository.findByIntentId(intentId) } returns existingOrder
            every { paymentService.retrieveIntent(intentId) } returns intent
            every { paymentService.updatePaymentIntent(intent, any()) } returns mockk()

            val order = orderService.submitOrder(userId, null, submitRequest())

            assertEquals(existingOrder.id, order.id)
            assertEquals(BigDecimal("105.00"), order.totalAmount)
        }

        @Test
        fun `submitOrder throws when cart has no intentId`() {
            every { cartService.getCart(userId, null) } returns Cart()

            assertThrows<AppException> { orderService.submitOrder(userId, null, submitRequest()) }
        }

        @Test
        fun `submitOrder throws when Stripe intent is already cancelled`() {
            val cart = cartWithItems(intentId, product(1, BigDecimal("100.00")) to 1)
            val cancelledIntent = mockIntent(status = "canceled")

            every { cartService.getCart(userId, null) } returns cart
            every { paymentService.retrieveIntent(intentId) } returns cancelledIntent

            assertThrows<AppException> { orderService.submitOrder(userId, null, submitRequest()) }
        }

        @Test
        fun `submitOrder throws when cart is empty`() {
            every { cartService.getCart(userId, null) } returns Cart(intentId = intentId)

            assertThrows<AppException> { orderService.submitOrder(userId, null, submitRequest()) }
        }

        @Test
        fun `submitOrder populates all items from cart`() {
            val cart =
                cartWithItems(intentId, product(1, BigDecimal("100.00")) to 1, product(2, BigDecimal("50.00")) to 2)
            val intent = mockIntent()

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(any()) } returns BigDecimal("5.00")
            every { orderRepository.findByIntentId(intentId) } returns null
            every { paymentService.retrieveIntent(intentId) } returns intent
            every { paymentService.updatePaymentIntent(intent, any()) } returns mockk()

            val order = orderService.submitOrder(userId, null, submitRequest())

            assertEquals(2, order.items.size)
        }

        @Test
        fun `submitOrder uses price snapshot not current product price`() {
            val p = product(1, BigDecimal("100.00"))
            val cart =
                Cart(intentId = intentId, shippingType = ShippingType.STANDARD, shippingCost = BigDecimal("5.00"))
            cart.addItem(CartItem(product = p, quantity = 1, priceSnapshot = BigDecimal("80.00")))

            val intent = mockIntent()

            every { cartService.getCart(userId, null) } returns cart
            every { cartService.calculateShippingCost(any()) } returns BigDecimal("5.00")
            every { orderRepository.findByIntentId(intentId) } returns null
            every { paymentService.retrieveIntent(intentId) } returns intent
            every { paymentService.updatePaymentIntent(intent, any()) } returns mockk()

            val order = orderService.submitOrder(userId, null, submitRequest())

            assertEquals(BigDecimal("80.00"), order.items.first().price)
            assertEquals(BigDecimal("85.00"), order.totalAmount)
        }
    }

    @Nested
    inner class PaymentOutcomes {

        @Test
        fun `handleSuccess sets order status to PAID`() {
            val p = product(1, BigDecimal("100.00"), stock = 5)
            val order = pendingOrder(p to 1)
            every { productService.get(1) } returns p
            every { cartService.getByPaymentIntentId(intentId) } returns Cart(intentId = intentId)
            every { cartService.delete(any()) } just Runs

            val result = orderService.handleSuccess(order, "ch_123")

            assertEquals(OrderStatus.PAID, result.status)
            assertEquals("ch_123", result.chargeId)
        }

        @Test
        fun `handleSuccess decrements product stock`() {
            val p = product(1, BigDecimal("100.00"), stock = 5)
            val order = pendingOrder(p to 2)
            every { productService.get(1) } returns p
            every { cartService.getByPaymentIntentId(intentId) } returns Cart(intentId = intentId)
            every { cartService.delete(any()) } just Runs

            orderService.handleSuccess(order, "ch_123")

            assertEquals(3, p.stock)
        }

        @Test
        fun `handleSuccess deletes cart after successful payment`() {
            val p = product(1, BigDecimal("100.00"), stock = 5)
            val cart = Cart(intentId = intentId)
            val order = pendingOrder(p to 1)
            every { productService.get(1) } returns p
            every { cartService.getByPaymentIntentId(intentId) } returns cart
            every { cartService.delete(any()) } just Runs

            orderService.handleSuccess(order, "ch_123")

            verify(exactly = 1) { cartService.delete(cart) }
        }

        @Test
        fun `handleSuccess refunds and cancels when stock is exhausted`() {
            val p = product(1, BigDecimal("100.00"), stock = 0)
            val order = pendingOrder(p to 1)
            every { productService.get(1) } returns p
            every { paymentService.refundCharge(any(), any()) } just Runs

            val result = orderService.handleSuccess(order, "ch_123")

            assertEquals(OrderStatus.CANCELLED, result.status)
            verify { paymentService.refundCharge("ch_123", BigDecimal("105.00")) }
        }

        @Test
        fun `handleCancel sets status to CANCELLED with reason`() {
            val p = product(1, BigDecimal("100.00"), stock = 5)
            val order = pendingOrder(p to 2)
            every { productService.save(any()) } returns mockk()

            val result = orderService.handleCancel(order, "expired")

            assertEquals(OrderStatus.CANCELLED, result.status)
            assertEquals("expired", result.failureReason)
        }

        @Test
        fun `handleCancel restocks all order items`() {
            val p = product(1, BigDecimal("100.00"), stock = 3)
            val order = pendingOrder(p to 2)
            every { productService.save(any()) } returns mockk()

            orderService.handleCancel(order, null)

            verify { productService.save(match { it.stock == 5 }) }
        }

        @Test
        fun `handleRefund sets status to REFUNDED with refundId`() {
            val order = Order(intentId = intentId, status = OrderStatus.PAID, totalAmount = BigDecimal("105.00"))

            val result = orderService.handleRefund(order, "re_123")

            assertEquals(OrderStatus.REFUNDED, result.status)
            assertEquals("re_123", result.refundId)
        }

        @Test
        fun `handleRefundFail sets status to REFUND_FAILED with reason`() {
            val order =
                Order(intentId = intentId, status = OrderStatus.REFUND_PENDING, totalAmount = BigDecimal("105.00"))

            val result = orderService.handleRefundFail(order, "gateway error")

            assertEquals(OrderStatus.REFUND_FAILED, result.status)
            assertEquals("gateway error", result.failureReason)
        }

        @Test
        fun `handleRefundFail truncates failure reason to 512 characters`() {
            val order =
                Order(intentId = intentId, status = OrderStatus.REFUND_PENDING, totalAmount = BigDecimal("105.00"))
            val longReason = "x".repeat(600)

            val result = orderService.handleRefundFail(order, longReason)

            assertEquals(512, result.failureReason?.length)
        }
    }

    @Nested
    inner class IsComplete {

        private fun contactWith(address: AddressFields) = Contact(
            name = "Test",
            email = "t@t.com",
            phone = "123",
            address = address,
        )

        @Test
        fun `isComplete returns false when items are empty`() {
            val order = Order(intentId = intentId, totalAmount = BigDecimal.ZERO)
            order.contact = contactWith(mockk())
            assertFalse(order.isComplete())
        }

        @Test
        fun `isComplete returns false when contact is null`() {
            val order = Order(intentId = intentId, totalAmount = BigDecimal.ZERO)
            order.addItem(
                OrderItem(
                    order = order,
                    product = product(1, BigDecimal.ONE),
                    productName = "p",
                    quantity = 1,
                    price = BigDecimal.ONE
                )
            )
            assertFalse(order.isComplete())
        }

        @Test
        fun `isComplete returns true when items and contact are present`() {
            val order = Order(intentId = intentId, totalAmount = BigDecimal.ZERO)
            order.contact = contactWith(mockk())
            order.addItem(
                OrderItem(
                    order = order,
                    product = product(1, BigDecimal.ONE),
                    productName = "p",
                    quantity = 1,
                    price = BigDecimal.ONE
                )
            )
            assertTrue(order.isComplete())
        }
    }
}