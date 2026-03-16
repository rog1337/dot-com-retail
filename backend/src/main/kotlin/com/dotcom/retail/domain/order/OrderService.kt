package com.dotcom.retail.domain.order

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.CartError
import com.dotcom.retail.common.exception.OrderError
import com.dotcom.retail.common.exception.ProductError
import com.dotcom.retail.common.exception.TransactionError
import com.dotcom.retail.common.model.AddressFields
import com.dotcom.retail.common.model.AuditSortOrder
import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.domain.user.Contact as ContactEntity
import com.dotcom.retail.common.service.EncryptionService
import com.dotcom.retail.domain.cart.CartService
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.order.dto.CheckoutResponse
import com.dotcom.retail.domain.order.dto.SubmitOrderRequest
import com.dotcom.retail.domain.payment.PaymentService
import com.dotcom.retail.domain.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class OrderService(
    private val cartService: CartService,
    private val userService: UserService,
    private val productService: ProductService,
    private val orderRepository: OrderRepository,
    private val paymentService: PaymentService,
    private val encryptionService: EncryptionService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 3_600_000)
    fun cancelAbandonedOrders() {
        log.info("Running scheduled abandoned order cleanup job")
        val cutoff = Instant.now().minus(Duration.ofHours(1))
        val abandoned = orderRepository.findAbandonedOrders(cutoff)

        if (abandoned.isEmpty()) return
        log.info("Cleanup job: cancelling ${abandoned.size} abandoned orders")

        abandoned.forEach { order ->
            try {
                paymentService.cancelPaymentIntent(order.intentId)
            } catch (ex: Exception) {
                log.error("Failed to cancel abandoned order=${order.id}", ex)
            }
        }
    }

    @Transactional
    fun submitOrder(userId: UUID?, sessionId: String?, request: SubmitOrderRequest): Order {
        val cart = cartService.getCart(userId, sessionId)
        if (cart.items.isEmpty()) throw AppException(CartError.CART_EMPTY)
        cartService.checkStock(cart)

        val intentId = cart.intentId
            ?: throw AppException(OrderError.ORDER_NOT_FOUND)

        val intent = paymentService.retrieveIntent(intentId)
        if (intent.status == "canceled") {
            throw AppException(TransactionError.PAYMENT_STATUS_CANCELLED)
        }

        val shippingType = request.shippingType
        val shippingCost = cartService.calculateShippingCost(shippingType)
        var totalAmount = shippingCost

        val order = findByPaymentIntentId(intentId) ?: Order(
            user = userId?.let { userService.getById(userId) },
            sessionId = sessionId,
            status = OrderStatus.PENDING_PAYMENT,
            shippingType = shippingType,
            shippingCost = shippingCost,
            totalAmount = shippingCost,
            intentId = intentId,
        )

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
            this.totalAmount = totalAmount
        }

        paymentService.updatePaymentIntent(intent, totalAmount)
        return save(order)
    }

    fun save(order: Order): Order {
        return orderRepository.save(order)
    }

    @Transactional
    fun handleSuccess(order: Order, chargeId: String?): Order {
        try {
            order.items.forEach {
                if (it.product.stock < it.quantity) {
                    throw AppException(ProductError.PRODUCT_INSUFFICIENT_STOCK.withIdentifier(it.product.id))
                }
                it.product.stock -= it.quantity
            }
        } catch (e: AppException) {
            if (e.code != ProductError.PRODUCT_INSUFFICIENT_STOCK.code)
                throw e

            log.error("Stock exhausted for order=${order.id} after payment — refunding")
            paymentService.refundCharge(chargeId!!, order.totalAmount)
            order.status = OrderStatus.CANCELLED
            order.failureReason = e.message
            return save(order)
        } catch (e: ObjectOptimisticLockingFailureException) {
            throw e
        }

        order.status = OrderStatus.PAID
        order.chargeId = chargeId
        val cart = cartService.getByPaymentIntentId(order.intentId)
        cartService.delete(cart)
        return save(order)
    }

    @Transactional
    fun handleCancel(order: Order, reason: String?): Order {
        order.status = OrderStatus.CANCELLED
        order.failureReason = reason?.take(512)
        restock(order)
        return save(order)
    }

    @Transactional
    fun handleRefund(order: Order, refundId: String?): Order {
        order.status = OrderStatus.REFUNDED
        order.refundId = refundId
        return save(order)
    }

    @Transactional
    fun handleRefundFail(order: Order, reason: String?): Order {
        order.status = OrderStatus.REFUND_FAILED
        order.failureReason = reason?.take(512)
        return save(order)
    }

    @Transactional
    fun restock(order: Order) {
        for (item in order.items) {
            val product = item.product
            product.stock += item.quantity
            productService.save(product)
        }
    }

    fun getOrders(userId: UUID?, sessionId: String?, status: OrderStatus?, sort: AuditSortOrder, page: Int, pageSize: Int): Page<Order> {
        val sort = if (sort == AuditSortOrder.desc) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page, pageSize, Sort.by(sort, "createdAt"))
        if (userId != null) return orderRepository.findByUserIdAndStatus(userId, status, pageable)
        if (sessionId != null) return orderRepository.findBySessionIdAndStatus(sessionId, status, pageable)
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

    fun findByPaymentIntentId(paymentIntentId: String): Order? {
        return orderRepository.findByIntentId(paymentIntentId)
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