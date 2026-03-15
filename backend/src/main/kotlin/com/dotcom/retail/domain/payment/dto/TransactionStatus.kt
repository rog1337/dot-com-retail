package com.dotcom.retail.domain.payment.dto

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUND_PENDING,
    REFUNDED,
    REFUND_FAILED,
    CANCELLED,
}