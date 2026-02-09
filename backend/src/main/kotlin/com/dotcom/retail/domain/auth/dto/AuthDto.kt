package com.dotcom.retail.domain.auth.dto

import com.dotcom.retail.domain.user.UserDto
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterResponse(
    val accessToken: String,
    val user: UserDto,
)

sealed class LoginResult () {
    data class Success(
        val accessToken: String,
        val refreshToken: String,
        val user: UserDto
    ) : LoginResult()

    class TwoFactorRequired : LoginResult()
}

data class LoginResponse(
    val accessToken: String,
    val user: UserDto,
)

data class LoginRequest(
    @field:NotBlank(message = "Email must not be empty")
    val email: String,

    @field:NotBlank(message = "Password must not be empty")
    val password: String,

    val twoFactorCode: String? = null
)

data class RefreshResponse(
    val accessToken: String
)

data class RegisterRequest(
    @field:NotBlank(message = "Email must not be empty")
    @field:Email(message = "Email must a valid email address")
    val email: String,

    @field:NotBlank(message = "Password must not be empty")
    @field:Size(min = 5, max = 100, message = "Password must be between 5 and 100 characters.")
    val password: String,

    @field:NotBlank(message = "Name must not be empty")
    @field:Size(min = 1, max = 30, message = "Name must be between 1 and 30 characters.")
    val displayName: String,

    @field:NotBlank(message = "Captcha is required")
    val captchaToken: String,
)