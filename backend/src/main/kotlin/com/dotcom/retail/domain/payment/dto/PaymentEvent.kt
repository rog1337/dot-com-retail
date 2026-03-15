package com.dotcom.retail.domain.payment.dto

import java.io.Serializable
import java.math.BigDecimal

data class PaymentEvent(
    val intentId: String,
    val status: TransactionStatus,
    val chargeId: String? = null,
    val refundId: String? = null,
    val failureReason: String? = null,
    val amount: BigDecimal,
    val currency: String,
    val retryCount: Int = 0,
) : Serializable