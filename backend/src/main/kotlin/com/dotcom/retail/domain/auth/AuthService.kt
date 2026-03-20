package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.exception.*
import com.dotcom.retail.common.model.TokenType
import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.config.properties.TurnstileProperties
import com.dotcom.retail.domain.auth.dto.*
import com.dotcom.retail.domain.user.Role
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserMapper
import com.dotcom.retail.domain.user.UserService
import com.dotcom.retail.domain.user.dto.CreateUserParams
import com.dotcom.retail.security.jwt.JwtService
import com.dotcom.retail.security.jwt.TokenPair
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import java.util.*
import java.util.concurrent.TimeUnit


@Service
class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProperties: JwtProperties,
    private val turnstileProperties: TurnstileProperties,
    private val restClientBuilder: RestClient.Builder,
    private val twoFactorAuthService: TwoFactorAuthService,
    private val userMapper: UserMapper
) {

    companion object {
        const val COOKIE_SAME_SITE_STRICT = "Strict"
        const val COOKIE_PATH = "/"
        const val TURNSTILE_ERROR_TOKEN_ALREADY_VALIDATED = "timeout-or-duplicate"
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


             if (response.body?.success == false) {
                 val errorCode = response.body?.errorCodes?.firstOrNull()
                 if (errorCode != null) {
                     when (errorCode) {
                         TURNSTILE_ERROR_TOKEN_ALREADY_VALIDATED ->
                             throw AppException(CaptchaError.CAPTCHA_TOKEN_ALREADY_USED)
                     }
                 }
                 false
             } else {
                 true
             }
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

         val tokenPair = jwtService.rotateTokens(user.id, user.role)

         return LoginResult.Success(
             accessToken = tokenPair.accessToken,
             refreshToken = tokenPair.refreshToken,
             user = userMapper.toDto(user)
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

        val role = try {
            Role.valueOf(claims.getValue(JwtService.TOKEN_ROLE_CLAIM).toString())
        } catch(_: NoSuchElementException) {
            throw AppException(JwtError.JWT_ROLE_MISSING)
        }

        return TokenPair(
            jwtService.rotateTokens(UUID.fromString(userId), role).accessToken,
            jwtService.rotateTokens(UUID.fromString(userId), role).refreshToken
        )
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
