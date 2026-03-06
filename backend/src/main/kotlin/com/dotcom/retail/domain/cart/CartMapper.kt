package com.dotcom.retail.domain.cart

import com.dotcom.retail.domain.cart.dto.CartDto
import com.dotcom.retail.domain.cart.dto.CartItemDto
import com.dotcom.retail.domain.catalogue.image.ImageMapper
import org.springframework.stereotype.Component

@Component
class CartMapper(
    private val imageMapper: ImageMapper
) {
    fun toDto(cart: Cart): CartDto = CartDto(
        id = cart.id,
        sessionId = cart.sessionId,
        items = cart.items.map { toCartItemDto(it) },
        subTotalPrice = cart.getSubTotalPrice(),
        totalPrice = cart.getTotalPrice(),
        shippingType = cart.shippingType,
        shippingCost = cart.shippingCost,
        totalQuantity = cart.getTotalQuantity()
    )

    fun toCartItemDto(cartItem: CartItem): CartItemDto {
        val image = cartItem.product.images.firstOrNull()
        return CartItemDto(
            productId = cartItem.product.id,
            productName = cartItem.product.name,
            imageUrl = image?.let { imageMapper.toCartImageDto(it) },
            price = cartItem.priceSnapshot,
            quantity = cartItem.quantity,
        )
    }
}