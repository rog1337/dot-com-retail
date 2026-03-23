package com.dotcom.retail.domain.admin.order.dto

import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.domain.order.OrderStatus
import com.dotcom.retail.domain.order.ShippingType
import com.dotcom.retail.domain.order.dto.OrderItemDto
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

data class AdminOrderDto(
    val id: UUID,
    val status: OrderStatus,
    val paymentId: String,
    val sessionId: String? = null,
    val items: List<OrderItemDto>,
    val shippingType: ShippingType?,
    val shippingCost: BigDecimal?,
    val totalAmount: BigDecimal,
    val contact: Contact?,
    val date: Long,
    val notes: String? = null,
)

data class OrderUpdateRequest(
    val orderId: UUID,
    val shippingType: ShippingType?,
    val status: OrderStatus?,
)

data class OrderCancelRequest(
    @field:Size(max = 512)
    val reason: String,
)