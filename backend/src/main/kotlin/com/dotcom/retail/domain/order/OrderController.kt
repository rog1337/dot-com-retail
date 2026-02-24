package com.dotcom.retail.domain.order

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.cart.CartService
import com.dotcom.retail.domain.order.dto.OrderRequest
import com.dotcom.retail.domain.order.dto.OrderResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiRoutes.Order.BASE)
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping
    fun placeOrder(
        @AuthenticationPrincipal userId: UUID?,
        @RequestHeader(CartService.SESSION_ID_HEADER, required = false) sessionId: String?,
        @Valid @RequestBody request: OrderRequest,
    ): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(userId, sessionId, request)
        return ResponseEntity.ok().body(order)
    }

}