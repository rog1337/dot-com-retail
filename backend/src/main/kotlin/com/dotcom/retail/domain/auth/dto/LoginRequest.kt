package com.dotcom.retail.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Email must not be empty")
    val email: String,

    @field:NotBlank(message = "Password must not be empty")
    val password: String,
)