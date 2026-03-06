package com.dotcom.retail.domain.cart

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.cart.dto.CartDto
import com.dotcom.retail.domain.cart.dto.CartUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(ApiRoutes.Cart.BASE)
class CartController(
    private val cartService: CartService,
    private val cartMapper: CartMapper,
) {

    @GetMapping
    fun getCart(
        @AuthenticationPrincipal userId: UUID?,
        @RequestHeader(CartService.SESSION_ID_HEADER, required = false) sessionId: String?,
    ): ResponseEntity<CartDto> {
        val cart = cartService.getActiveCart(userId, sessionId)
        return ResponseEntity.ok().body(cartMapper.toDto(cart))
    }

    @PutMapping
    fun update(
        @AuthenticationPrincipal userId: UUID?,
        @RequestHeader(CartService.SESSION_ID_HEADER, required = false) sessionId: String?,
        @RequestBody request: CartUpdateRequest
    ): ResponseEntity<CartDto> {
        val cart = cartService.update(userId, sessionId, request)
        return ResponseEntity.ok().body(cartMapper.toDto(cart))
    }
}