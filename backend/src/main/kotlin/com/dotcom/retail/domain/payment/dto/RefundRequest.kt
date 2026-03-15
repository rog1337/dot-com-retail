package com.dotcom.retail.domain.payment.dto

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class RefundRequest(
    @field:NotNull
    val orderId: UUID,
    val reason: String? = null
)
