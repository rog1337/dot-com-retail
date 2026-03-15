package com.dotcom.retail.domain.order

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.cart.CartService
import com.dotcom.retail.domain.order.dto.OrderDto
import com.dotcom.retail.domain.order.dto.SubmitOrderRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(ApiRoutes.Order.BASE)
class OrderController(
    private val orderService: OrderService,
    private val orderMapper: OrderMapper,
) {

    @GetMapping
    fun getOrder(
        @PathVariable(required = false) id: UUID? = null,
        @RequestParam(required = false) paymentIntentId: String? = null,
    ): ResponseEntity<OrderDto> {
        val order = orderService.getOrder(id, paymentIntentId)
        return ResponseEntity.ok(orderMapper.toDto(order))
    }

    @PostMapping(ApiRoutes.Order.SUBMIT)
    fun submitOrder(
        @AuthenticationPrincipal userId: UUID?,
        @RequestHeader(CartService.SESSION_ID_HEADER, required = false) sessionId: String?,
        @Valid @RequestBody request: SubmitOrderRequest,
    ): ResponseEntity<OrderDto> {
        val order = orderService.submitOrder(userId, sessionId, request)
        return ResponseEntity.ok().body(orderMapper.toDto(order))
    }
}