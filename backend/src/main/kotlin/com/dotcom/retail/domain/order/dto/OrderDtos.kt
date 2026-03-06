package com.dotcom.retail.domain.order.dto

import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.domain.cart.dto.CartDto
import com.dotcom.retail.domain.order.OrderStatus
import com.dotcom.retail.domain.order.ShippingType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

class SubmitOrderRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val phone: String,
    @field:NotBlank @field:Email
    val email: String,

    @field:NotNull
    val address: OrderAddress,

    @field:NotNull
    val shippingType: ShippingType,
    val notes: String? = null,
)

data class OrderAddress(
    @field:NotBlank
    val streetLine1: String,
    val streetLine2: String?,
    @field:NotBlank
    val city: String,
    val stateOrProvince: String?,
    val postalCode: String,
    val country: String,
)

data class CreateOrderResponse(
    val orderId: String,
    val status: OrderStatus,
    val clientSecret: String,
)

data class OrderDto(
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

data class OrderItemDto(
    val productId: Long,
    val productName: String,
    val imageUrl: String?,
    val price: BigDecimal,
    val quantity: Int,
    val totalAmount: BigDecimal,
)