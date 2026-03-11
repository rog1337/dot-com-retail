package com.dotcom.retail.domain.order

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.CartError
import com.dotcom.retail.common.exception.OrderError
import com.dotcom.retail.common.model.AddressFields
import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.domain.user.Contact as ContactEntity
import com.dotcom.retail.common.service.EncryptionService
import com.dotcom.retail.domain.cart.CartMapper
import com.dotcom.retail.domain.cart.CartService
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.order.dto.CreateOrderResponse
import com.dotcom.retail.domain.order.dto.SubmitOrderRequest
import com.dotcom.retail.domain.payment.PaymentService
import com.dotcom.retail.domain.user.UserService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class OrderService(
    private val cartService: CartService,
    private val userService: UserService,
    private val productService: ProductService,
    private val orderRepository: OrderRepository,
    private val paymentService: PaymentService,
    private val cartMapper: CartMapper,
    private val encryptionService: EncryptionService
) {
    @Transactional
    fun createOrder(userId: UUID?, sessionId: String?): CreateOrderResponse {
        val cart = cartService.getCartWithLock(userId, sessionId)
        if (cart.items.isEmpty()) throw AppException(CartError.CART_EMPTY)
        cartService.checkStock(cart)

        val intentId = cart.intentId
        if (intentId != null) {
            val order = getByPaymentIntentId(intentId)
            if (order.status == OrderStatus.PENDING_PAYMENT) {
                val intent = paymentService.retrieveIntent(intentId)
                return CreateOrderResponse(
                    orderId = order.id.toString(),
                    status = order.status,
                    clientSecret = intent.clientSecret
                )
            }
        }

        val shippingCost = cartService.calculateShippingCost(ShippingType.STANDARD)
        val order = Order(
            user = userId?.let { userService.getById(userId) },
            sessionId = sessionId,
            status = OrderStatus.PENDING_PAYMENT,
            shippingCost = shippingCost,
            totalAmount = shippingCost,
            intentId = "placeholder"
        )

        cart.shippingType = ShippingType.STANDARD
        cart.shippingCost = shippingCost

        val amount = cart.getTotalPrice()
        val paymentIntent = paymentService.createPaymentIntent(amount)
        order.intentId = paymentIntent.id
        cart.intentId = paymentIntent.id
        cartService.save(cart)
        save(order)

        return CreateOrderResponse(
            orderId = order.id.toString(),
            status = order.status,
            clientSecret = paymentIntent.clientSecret,
        )
    }

    @Transactional
    fun submitOrder(userId: UUID?, sessionId: String?, request: SubmitOrderRequest): Order {
        val cart = cartService.getCart(userId, sessionId)
        if (cart.items.isEmpty()) throw AppException(CartError.CART_EMPTY)
        cartService.checkStock(cart)

        val intentId = cart.intentId
            ?: throw AppException(OrderError.ORDER_NOT_FOUND)

        val order = getByPaymentIntentId(intentId)

        val email = request.email

        val requestAddress = request.address

        val address = AddressFields(
            streetLine1 = encryptionService.encrypt(requestAddress.streetLine1),
            streetLine2 = requestAddress.streetLine2?.let { encryptionService.encrypt(it) },
            city = encryptionService.encrypt(requestAddress.city),
            stateOrProvince = requestAddress.stateOrProvince?.let { encryptionService.encrypt(it) },
            postalCode = encryptionService.encrypt(requestAddress.postalCode),
            country = encryptionService.encrypt(requestAddress.country),
        )
        val contact = Contact(
            name = encryptionService.encrypt(request.name),
            email = encryptionService.encrypt(email),
            phone = encryptionService.encrypt(request.phone),
            address = address
        )
        order.contact = contact
        order.notes = request.notes?.let { encryptionService.encrypt(it) }

        val user = userId?.let { userService.findById(userId) }
        user?.let {
            val contact = ContactEntity(contact = contact)
            it.contact = contact
            userService.save(it)
        }

        val shippingType = request.shippingType
        val shippingCost = cartService.calculateShippingCost(shippingType)
        var totalAmount = shippingCost

        val orderItems = cart.items.map {
            totalAmount = totalAmount.add(it.getTotalPrice())
            OrderItem(
                order = order,
                product = it.product,
                productName = it.product.name,
                quantity = it.quantity,
                price = it.priceSnapshot,
            )
        } as MutableList<OrderItem>

        order.items.clear()
        order.items.addAll(orderItems)

        order.apply {
            this.shippingType = shippingType
            this.shippingCost = shippingCost
            this.totalAmount = totalAmount
        }

        return save(order)
    }

    fun save(order: Order): Order {

        return orderRepository.save(order)
    }

    fun handleSuccess(order: Order): Order {
        order.status = OrderStatus.PAID
        val cart = cartService.getByPaymentIntentId(order.intentId)
        cartService.delete(cart)
        return save(order)
    }

    fun handleFail(order: Order): Order {
        order.status = OrderStatus.FAILED
        restock(order)
        return save(order)
    }

    fun handleCancel(order: Order): Order {
        order.status = OrderStatus.CANCELLED
        restock(order)
        return save(order)
    }

    fun restock(order: Order) {
        for (item in order.items) {
            val product = item.product
            product.stock += item.quantity
            productService.save(product)
        }
    }

    fun getOrders(userId: UUID?, sessionId: String?): Set<Order> {
        if (userId != null) {
            val orders = findByUserId(userId)
            if (orders.isNotEmpty()) return orders
            throw AppException(OrderError.ORDER_NOT_FOUND.withIdentifier(userId))
        }
        if (sessionId != null) {
            val orders = findBySessionId(sessionId)
            if (orders.isNotEmpty()) return orders
            throw AppException(OrderError.ORDER_NOT_FOUND.withIdentifier(sessionId))
        }

        throw AppException(CartError.CART_IDENTIFIER_REQUIRED)
    }

    fun findOrders(userId: UUID?, sessionId: String?): Set<Order> {
        if (userId != null) return findByUserId(userId)
        if (sessionId != null) return findBySessionId(sessionId)
        throw AppException(CartError.CART_IDENTIFIER_REQUIRED)
    }

    fun findByUserId(userId: UUID): Set<Order> {
        return orderRepository.findByUserId(userId)
    }

    fun findBySessionId(sessionId: String): Set<Order> {
        return orderRepository.findBySessionId(sessionId)
    }

    fun getByPaymentIntentId(intentId: String): Order {
        return orderRepository.findByIntentId(intentId) ?: throw AppException(OrderError.ORDER_NOT_FOUND.withIdentifier(intentId))
    }

    fun getOrder(orderId: UUID?, paymentIntentId: String?): Order {
        if (orderId != null) return getById(orderId)
        if (paymentIntentId != null) return getByPaymentIntentId(paymentIntentId)
        throw AppException(OrderError.ORDER_NOT_FOUND)
    }

    fun getById(orderId: UUID): Order {
        return orderRepository.findByIdOrNull(orderId)
            ?: throw AppException(OrderError.ORDER_NOT_FOUND.withIdentifier(orderId))
    }

}