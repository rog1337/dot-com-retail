package com.dotcom.retail.domain.payment

import com.dotcom.retail.common.constants.ApiRoutes
import org.springframework.web.bind.annotation.*

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
}