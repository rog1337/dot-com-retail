package com.dotcom.retail.domain.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Email must not be empty")
    val email: String,

    @field:NotBlank(message = "Password must not be empty")
    val password: String,
    @field:NotBlank(message = "Password must not be empty")
    val confirmPassword: String,

    @field:NotBlank(message = "Name must not be empty")
    @field:Size(min = 1, max = 16, message = "Name must be between 1 and 30 characters.")
    val displayName: String,

    @field:NotBlank(message = "Captcha is required")
    val captchaToken: String,
)