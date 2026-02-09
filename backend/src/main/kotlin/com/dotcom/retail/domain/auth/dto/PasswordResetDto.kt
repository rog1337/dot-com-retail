package com.dotcom.retail.domain.auth.dto

data class PasswordResetRequest(
    val email: String,
)

data class PasswordResetVerification(
    val token: String,
    val password: String
)