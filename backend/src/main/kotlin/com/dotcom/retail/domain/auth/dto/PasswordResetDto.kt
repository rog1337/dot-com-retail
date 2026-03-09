package com.dotcom.retail.domain.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PasswordResetRequest(
    @field:Email
    val email: String,
)

data class PasswordResetVerification(
    @field:NotBlank
    val token: String,
    @field:Size(min = 6, max = 80)
    val password: String
)