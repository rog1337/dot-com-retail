package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus

open class AuthException(
    message: String = "Authentication failed",
    status: HttpStatus = HttpStatus.UNAUTHORIZED,
    cause: Throwable? = null
) : AppException(message, status, cause) {

    companion object {
        fun invalidLogin() = AuthException("Invalid email or password")
        fun incorrectPassword() = AuthException("Incorrect password")
        fun nonLocalAccount() = AuthException("Register or log in with an authentication provider to access this account")

        fun captchaFailed() = AuthException("Captcha verification failed")
    }
}

open class OAuthException(
    provider: String = "OAuth2",
    details: String = "Unexpected error occurred"
) : AuthException("Login with $provider failed: $details") {

    companion object {
        fun unknownProvider() = OAuthException(details = "Unknown provider")
        fun emailNotVerified(provider: String) = OAuthException(provider, "Email is not verified")
    }
}





