package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus

open class AuthException(
    message: String = "Authentication failed",
    status: HttpStatus = HttpStatus.UNAUTHORIZED,
    cause: Throwable? = null
) : AppException(message, status, cause)

class IncorrectPasswordException : AuthException("Incorrect password")
class NonLocalAccountException : AuthException("Register or log in with an authentication provider to access this account")
class InvalidLoginException : AuthException("Invalid email or password")

open class OAuthException(
    provider: String = "OAuth2",
    details: String = "Unexpected error occurred"
) : AuthException("Login with $provider failed: $details")

class UnknownOAuthProviderException : OAuthException(details = "Unknown provider")
class OAuthEmailNotVerifiedException(provider: String) : OAuthException(provider, "Email is not verified")

class CaptchaVerificationException() : AuthException("Captcha verification failed")






