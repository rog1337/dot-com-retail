package com.dotcom.retail.common.service


import com.dotcom.retail.domain.contact.ContactRequest
import com.dotcom.retail.domain.order.Order

interface EmailService {
    fun sendContactEmail(request: ContactRequest)
    fun sendOrderCancelled(order: Order)
    fun sendOrderUpdated(order: Order)
    fun sendReviewRemoved(email: String, reviewId: Long, reviewBody: String?, cause: String?)
    fun sendOrderConfirmation(order: Order)
    fun sendPaymentFailed(order: Order)
    fun sendRefundConfirmation(order: Order)
    fun sendRefundFailed(order: Order)
    fun sendPasswordReset(to: String, token: String)
}