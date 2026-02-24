package com.dotcom.retail.domain.cart

import com.dotcom.retail.domain.cart.dto.CartDto
import com.dotcom.retail.domain.cart.dto.CartItemDto

object CartMapper {
    fun toDto(cart: Cart): CartDto = CartDto(
        id = cart.id,
        items = cart.items.map { toCartItemDto(it) },
        totalPrice = cart.getTotalPrice(),
    )

    fun toCartItemDto(cartItem: CartItem): CartItemDto = CartItemDto(
        productId = cartItem.product.id,
        productName = cartItem.product.name,
        price = cartItem.product.price,
        quantity = cartItem.quantity,
    )
}