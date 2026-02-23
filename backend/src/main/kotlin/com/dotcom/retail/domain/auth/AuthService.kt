package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.AuthError
import com.dotcom.retail.common.exception.CaptchaError
import com.dotcom.retail.common.exception.JwtError
import com.dotcom.retail.common.exception.TwoFactorAuthError
import com.dotcom.retail.common.exception.UserError
import com.dotcom.retail.common.model.TokenType
import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.config.properties.TurnstileProperties
import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.LoginResult
import com.dotcom.retail.domain.auth.dto.RegisterOAuthUser
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.security.jwt.TokenPair
import com.dotcom.retail.domain.auth.dto.TurnstileResponse
import com.dotcom.retail.domain.user.CreateUserParams
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserService
import com.dotcom.retail.domain.user.toDto
import com.dotcom.retail.security.jwt.JwtService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import java.util.UUID
import java.util.concurrent.TimeUnit


@Service
class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProperties: JwtProperties,
    private val turnstileProperties: TurnstileProperties,
    private val restClientBuilder: RestClient.Builder,
    private val twoFactorAuthService: TwoFactorAuthService
) {

    companion object {
        const val COOKIE_SAME_SITE_STRICT = "Strict"
        const val COOKIE_PATH = "/"
    }

    private val turnstileClient: RestClient by lazy {
        restClientBuilder.baseUrl(turnstileProperties.verifyUrl).build()
    }

    fun register(request: RegisterRequest): User {
        if (!verifyCaptcha(request.captchaToken)) throw AppException(CaptchaError.CAPTCHA_FAILED)

        val user = userService.create(
            CreateUserParams(
                request.email,
                request.displayName,
                request.password
            )
        )

        return user
    }

     fun verifyCaptcha(token: String): Boolean {
         val formData = LinkedMultiValueMap<String, String>().apply {
             add("secret", turnstileProperties.secretKey)
             add("response", token)
         }

         return try {
             val response = turnstileClient.post()
                 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                 .body(formData)
                 .retrieve()
                 .toEntity<TurnstileResponse>()

             response.body?.success == true
         } catch (_: Exception) {
             false
         }
    }

    fun registerOAuthUser(details: RegisterOAuthUser): User {
        val user = User(
            email = details.email,
            displayName = details.displayName,
        )

        return userService.save(user)
    }

     fun login(request: LoginRequest): LoginResult {
         val user = userService.findByEmail(request.email) ?: throw AppException(UserError.USER_NOT_FOUND.withIdentifier(request.email))
         val storedPassword = user.password

         if (!passwordEncoder.matches(request.password, storedPassword)) {
             if (storedPassword.isNullOrEmpty()) throw AppException(AuthError.NON_LOCAL_ACCOUNT.withIdentifier(request.email))

             throw AppException(AuthError.INVALID_CREDENTIALS)
         }

         if (user.twoFactorEnabled) {
             if (request.twoFactorCode.isNullOrBlank()) {
                 return LoginResult.TwoFactorRequired()
             }

             val secret = user.twoFactorSecret ?: throw AppException(TwoFactorAuthError.TWO_FACTOR_SECRET_NOT_SET)

             if (!twoFactorAuthService.verifyCode(secret, request.twoFactorCode))
                 throw AppException(TwoFactorAuthError.INVALID_TWO_FACTOR_CODE)
         }

         val tokenPair = jwtService.rotateTokens(user.id)

         return LoginResult.Success(
             accessToken = tokenPair.accessToken,
             refreshToken = tokenPair.refreshToken,
             user = user.toDto()
         )
     }

    fun refreshTokens(request: HttpServletRequest): TokenPair {
        val refreshToken = jwtService.extractJwtFromCookie(request) ?: throw AppException(JwtError.JWT_REFRESH_MISSING)
        val claims = jwtService.validateTokenAndExtractClaims(refreshToken)

        val type = claims.getValue(JwtService.TOKEN_TYPE_CLAIM).toString()
        if (!jwtService.isRefreshToken(type)) throw AppException(JwtError.JWT_REFRESH_MISSING)

        val userId = claims.subject
        val version = claims.getValue(JwtService.TOKEN_VERSION_CLAIM).toString()
        if (!jwtService.isValidTokenVersion(userId, version)) throw AppException(JwtError.JWT_REFRESH_REVOKED)

        return TokenPair(jwtService.rotateTokens(UUID.fromString(userId)).accessToken, jwtService.rotateTokens(UUID.fromString(userId)).refreshToken)
    }

     fun createRefreshTokenCookie(refreshToken: String): ResponseCookie {
         val maxAgeSeconds = TimeUnit.MILLISECONDS.toSeconds(jwtProperties.refresh.exp)
         return ResponseCookie.from(TokenType.REFRESH, refreshToken)
            .httpOnly(true)
            .secure(true)
            .maxAge(maxAgeSeconds)
            .sameSite(COOKIE_SAME_SITE_STRICT)
            .path(COOKIE_PATH)
            .build()
    }

    fun logout(userId: UUID) {
        jwtService.revokeTokens(userId)
    }

    fun removeRefreshTokenCookie(): ResponseCookie {
        return ResponseCookie.from(TokenType.REFRESH, "")
            .httpOnly(true)
            .secure(true)
            .maxAge(0)
            .sameSite(COOKIE_SAME_SITE_STRICT)
            .path(COOKIE_PATH)
            .build()
    }
}
