package com.dotcom.retail.domain.cart.dto

import java.math.BigDecimal
import java.util.UUID

data class CartUpdateRequest(
    val productId: Long,
    val quantity: Int
)

data class CartDto(
    val id: UUID?,
    val items: List<CartItemDto>,
    val totalPrice: BigDecimal,
)

data class CartItemDto(
    val productId: Long,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int,
)

data class CartDetailsResponse(
    val cart: CartDto,
    val recommendedProductIds: List<Long>,
)