package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.exception.AuthException
import com.dotcom.retail.common.exception.NotFoundException
import com.dotcom.retail.common.model.TokenType
import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.config.properties.TurnstileProperties
import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.RegisterOAuthUser
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.auth.dto.TurnstileResponse
import com.dotcom.retail.domain.user.CreateUserParams
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserService
import com.dotcom.retail.security.jwt.JwtService
import io.jsonwebtoken.JwtException
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
    private val restClientBuilder: RestClient.Builder
) {

    companion object {
        const val COOKIE_SAME_SITE_STRICT = "Strict"
        const val COOKIE_PATH = "/"
        const val COOKIE_HEADER_NAME = "Set-Cookie"
    }

    private val turnstileClient: RestClient by lazy {
        restClientBuilder.baseUrl(turnstileProperties.verifyUrl).build()
    }

     fun register(request: RegisterRequest): User {
        if (!verifyCaptcha(request.captchaToken)) throw AuthException.captchaFailed()

        val user = userService.create(CreateUserParams(
            request.email,
            request.displayName,
            request.password
        ))
        issueNewTokens(user)

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

        return issueNewTokens(user)
    }

     fun login(request: LoginRequest): User {
        val user = userService.findByEmail(request.email) ?: throw NotFoundException(User::class.simpleName, request.email)
        val userPw = user.password

        if (!passwordEncoder.matches(request.password, userPw)) {
            if (userPw.isNullOrEmpty()) throw AuthException.nonLocalAccount()

            throw AuthException.incorrectPassword()
        }

        issueNewTokens(user)
        return user
    }

     fun refresh(req: HttpServletRequest): User {
        val refreshToken = jwtService.extractJwtFromCookie(req)
        if (refreshToken.isNullOrBlank()) throw JwtException("")

        val claims = jwtService.extractAndValidateClaims(refreshToken)
        if (!claims.getValue(JwtService.TOKEN_TYPE_CLAIM).equals(TokenType.REFRESH.value))
            throw JwtException("")

        val userId = claims.subject
        val user = userService.getById(UUID.fromString(userId))

        issueNewTokens(user)

        return user
    }

     fun createRefreshTokenCookie(refreshToken: String): ResponseCookie {
         val maxAgeSeconds = TimeUnit.MILLISECONDS.toSeconds(jwtProperties.refresh.exp)
         return ResponseCookie.from(TokenType.REFRESH.value, refreshToken)
            .httpOnly(true)
            .secure(true)
            .maxAge(maxAgeSeconds)
            .sameSite(COOKIE_SAME_SITE_STRICT)
            .path(COOKIE_PATH)
            .build()
    }

     fun issueNewTokens(user: User): User {
        user.accessToken = jwtService.generateAccessToken(user)
        user.refreshToken = jwtService.generateRefreshToken(user)
        return userService.save(user)
    }
}
