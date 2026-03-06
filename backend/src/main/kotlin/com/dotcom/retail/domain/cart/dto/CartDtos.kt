package com.dotcom.retail.domain.cart.dto

import com.dotcom.retail.domain.order.ShippingType
import java.math.BigDecimal
import java.util.UUID

data class CartUpdateRequest(
    val items: Set<ItemUpdateRequest>?,
    val shippingType: ShippingType?,
)

data class ItemUpdateRequest(
    val productId: Long,
    val quantity: Int
)

data class CartDto(
    val id: UUID?,
    val sessionId: String?,
    val items: List<CartItemDto>,
    val subTotalPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val shippingType: ShippingType?,
    val shippingCost: BigDecimal?,
    val totalQuantity: Int
)

data class CartItemDto(
    val productId: Long,
    val productName: String,
    val imageUrl: String?,
    val price: BigDecimal,
    val quantity: Int,
)