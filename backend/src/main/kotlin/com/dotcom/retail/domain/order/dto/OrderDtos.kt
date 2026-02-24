package com.dotcom.retail.domain.order.dto

import com.dotcom.retail.domain.order.OrderStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class OrderRequest(
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank
    val shippingName: String,
    @field:NotBlank
    val shippingAddress: String,
)

data class OrderResponse(
    val orderId: String,
    val status: OrderStatus,
    val clientSecret: String,
)