package com.dotcom.retail.domain.order

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.CartError
import com.dotcom.retail.common.exception.ProductError
import com.dotcom.retail.domain.cart.CartService
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.order.dto.OrderRequest
import com.dotcom.retail.domain.order.dto.OrderResponse
import com.dotcom.retail.domain.payment.PaymentService
import com.dotcom.retail.domain.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class OrderService(
    private val cartService: CartService,
    private val userService: UserService,
    private val productService: ProductService,
    private val orderRepository: OrderRepository,
    private val paymentService: PaymentService
) {

    @Transactional
    fun createOrder(userId: UUID?, sessionId: String?, request: OrderRequest): OrderResponse {
        val cart = cartService.getCart(userId, sessionId)
        if (cart.items.isEmpty()) throw AppException(CartError.CART_EMPTY)

        val order = Order(
            user = userId?.let { userService.getById(userId) },
            email = request.email,
            shippingName = request.shippingName,
            shippingAddress = request.shippingAddress,
            status = OrderStatus.PENDING_PAYMENT,
            totalAmount = BigDecimal.ZERO,
        )

        cart.items.forEach {
            val product = it.product

            if (product.stock < it.quantity) throw AppException(ProductError.PRODUCT_INSUFFICIENT_STOCK.withIdentifier(product.id))

            product.stock -= it.quantity
            productService.save(product)

            val orderItem = OrderItem(
                order = order,
                product = product,
                quantity = it.quantity,
                price = product.price,
            )

            order.addItem(orderItem)
            order.totalAmount = order.totalAmount.add(it.getTotalPrice())
        }
        save(order)

        val paymentIntent = paymentService.createPaymentIntent(order)

        cartService.clearCart(cart)

        return OrderResponse(
            orderId = order.id.toString(),
            status = order.status,
            clientSecret = paymentIntent.clientSecret,
        )
    }

    fun save(order: Order): Order {
        return orderRepository.save(order)
    }

    fun handleSuccess(order: Order): Order {
        order.status = OrderStatus.PAID
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

}