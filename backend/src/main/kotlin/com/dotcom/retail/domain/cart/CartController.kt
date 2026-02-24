package com.dotcom.retail.domain.cart

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.cart.dto.CartDetailsResponse
import com.dotcom.retail.domain.cart.dto.CartDto
import com.dotcom.retail.domain.cart.dto.CartUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiRoutes.Cart.BASE)
class CartController(private val cartService: CartService) {

//    @PostMapping
//    fun createCart(
//        @AuthenticationPrincipal userId: UUID?,
//        @RequestHeader(CartService.SESSION_ID_HEADER, required = false) sessionId: String?,
//        @RequestBody productIds: List<Long>?
//    ) {
//        val cart = cartService.createCart(userId, sessionId, productIds)
////        return ResponseEntity<>
//    }

    @GetMapping
    fun getCart(
        @AuthenticationPrincipal userId: UUID?,
        @RequestHeader(CartService.SESSION_ID_HEADER, required = false) sessionId: String?,
    ): ResponseEntity<CartDto> {
        val cart = cartService.getActiveCart(userId, sessionId)
        return ResponseEntity.ok().body(CartMapper.toDto(cart))
    }

    @PutMapping
    fun update(
        @AuthenticationPrincipal userId: UUID?,
        @RequestHeader(CartService.SESSION_ID_HEADER, required = false) sessionId: String?,
        @RequestBody request: List<CartUpdateRequest>?
    ): ResponseEntity<CartDto> {
        val cart = cartService.update(userId, sessionId, request)
        return ResponseEntity.ok().body(CartMapper.toDto(cart))
    }
}