package com.dotcom.retail.common.service

import com.dotcom.retail.domain.contact.ContactRequest
import com.dotcom.retail.domain.order.Order
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("loadtest")
@Primary
class LoadTestEmailService : EmailService {
    override fun sendContactEmail(request: ContactRequest) {}
    override fun sendOrderCancelled(order: Order) {}
    override fun sendOrderUpdated(order: Order) {}
    override fun sendReviewRemoved(email: String, reviewId: Long, reviewBody: String?, cause: String?) {}
    override fun sendOrderConfirmation(order: Order) {}
    override fun sendPaymentFailed(order: Order) {}
    override fun sendRefundConfirmation(order: Order) {}
    override fun sendRefundFailed(order: Order) {}
    override fun sendPasswordReset(to: String, token: String) {}
}