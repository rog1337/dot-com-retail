package com.dotcom.retail.auth

import com.dotcom.retail.user.UserResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Email must not be empty")
    val email: String,

    @field:NotBlank(message = "Password must not be empty")
    val password: String,

    @field:NotBlank(message = "First name must not be empty")
    @field:Size(min = 1, max = 16, message = "Name must be between 1 and 16 characters.")
    val displayName: String,
)

data class LoginRequest(
    @field:NotBlank(message = "Email must not be empty")
    val email: String,

    @field:NotBlank(message = "Password must not be empty")
    val password: String,
)

data class AuthResponse(
    val accessToken: String,
    val user: UserResponse,
)
