package com.dotcom.retail.domain.admin.order

import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.common.service.EncryptionService
import com.dotcom.retail.domain.admin.order.dto.OrderCancelRequest
import com.dotcom.retail.domain.admin.order.dto.OrderUpdateRequest
import com.dotcom.retail.domain.order.Order
import com.dotcom.retail.domain.order.OrderRepository
import com.dotcom.retail.domain.order.OrderService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdminOrderService(
    private val orderService: OrderService,
    private val orderRepository: OrderRepository,
    private val emailService: EmailService,
    private val encryptionService: EncryptionService
) {

    @Transactional
    fun updateOrder(orderId: UUID, request: OrderUpdateRequest): Order {
        val order = orderService.getById(orderId)
        request.status?.let { order.status = it }
        request.shippingType?.let { order.shippingType = it }
        orderRepository.save(order)
        emailService.sendOrderUpdated(order)
        return order
    }

    fun cancelOrder(orderId: UUID, request: OrderCancelRequest) {
        val order = orderService.getById(orderId)
        orderService.handleCancel(order, request.reason)
        emailService.sendOrderCancelled(order)
    }
}