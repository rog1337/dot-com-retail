package com.dotcom.retail.domain.payment

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.cart.CartService
import com.dotcom.retail.domain.payment.dto.RefundRequest
import com.stripe.model.Event
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
@RequestMapping(ApiRoutes.Payment.BASE)
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping(ApiRoutes.Payment.STRIPE)
    fun stripeEvent(
        @RequestBody payload: String,
        @RequestHeader(PaymentService.STRIPE_SIGNATURE_HEADER) sigHeader: String
    ) {
        paymentService.handleStripeEvent(payload, sigHeader)
    }

    @PostMapping(ApiRoutes.Payment.REFUND)
    fun refund(
        @AuthenticationPrincipal userId: UUID?,
        @RequestHeader(CartService.SESSION_ID_HEADER, required = false) sessionId: String?,
        @Valid @RequestBody request: RefundRequest,
    ): ResponseEntity<Void> {
        paymentService.initiateRefund(request.orderId, userId, sessionId, request.reason)
        return ResponseEntity.accepted().build()
    }
}