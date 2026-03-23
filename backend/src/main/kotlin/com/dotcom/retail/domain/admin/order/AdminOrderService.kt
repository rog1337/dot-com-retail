package com.dotcom.retail.domain.admin.order

import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.domain.admin.order.dto.OrderUpdateRequest
import com.dotcom.retail.domain.order.Order
import com.dotcom.retail.domain.order.OrderRepository
import com.dotcom.retail.domain.order.OrderService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminOrderService(
    private val orderService: OrderService,
    private val orderRepository: OrderRepository,
    private val emailService: EmailService
) {

    @Transactional
    fun updateOrder(orderId: UUID, request: OrderUpdateRequest): Order {
        val order = orderService.getById(orderId)
        request.status?.let { order.status = it }
        request.shippingType?.let { order.shippingType = it }
        orderRepository.save(order)

        val email = order.contact?.email
        if (email == null) {
            order.user?.email?.let { emailService.sendOrderUpdated(it, order) }
        } else {
            emailService.sendOrderUpdated(email, order)
        }
        return order
    }
}