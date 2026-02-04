package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.constants.SecurityConstants
import com.dotcom.retail.common.constants.SecurityConstants.COOKIE_PATH
import com.dotcom.retail.common.constants.SecurityConstants.COOKIE_SAME_SITE_STRICT
import com.dotcom.retail.common.constants.SecurityConstants.REFRESH_TOKEN_TYPE
import com.dotcom.retail.common.constants.SecurityConstants.TURNSTILE_VERIFY_URL
import com.dotcom.retail.common.exception.CaptchaVerificationException
import com.dotcom.retail.common.exception.EmailNotFoundException
import com.dotcom.retail.common.exception.IncorrectPasswordException
import com.dotcom.retail.common.exception.NonLocalAccountException
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
import java.util.UUID


@Service
class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProperties: JwtProperties,
    private val turnstileProperties: TurnstileProperties,
    private val restClientBuilder: RestClient.Builder
) {

     fun register(request: RegisterRequest): User {
        if (!verifyCaptcha(request.captchaToken)) throw CaptchaVerificationException()

        val user = userService.create(CreateUserParams(
            request.email,
            request.displayName,
            request.password
        ))
        setNewJwts(user)

        return user
    }

     fun verifyCaptcha(token: String): Boolean {
        val formData = LinkedMultiValueMap<String, String>()
        formData.add("secret", turnstileProperties.secretKey)
        formData.add("response", token)

        val request = restClientBuilder.baseUrl(TURNSTILE_VERIFY_URL).build()

        try {
            val response = request.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toEntity(TurnstileResponse::class.java)
            val body = response.body
            return body != null && body.success
        } catch (e: Exception) {
            return false
        }

//        val body = response.body
//        return body != null && body.success
    }

     fun registerOAuthUser(details: RegisterOAuthUser): User {
        val user = User(
            email = details.email,
            displayName = details.displayName,
        )

        return setNewJwts(user)
    }

     fun login(request: LoginRequest): User {
        val user = userService.findByEmail(request.email) ?: throw EmailNotFoundException(request.email)
        val userPw = user.password

        if (!passwordEncoder.matches(request.password, userPw)) {
            if (userPw.isNullOrEmpty()) throw NonLocalAccountException()

            throw IncorrectPasswordException()
        }

        setNewJwts(user)
        return user
    }

     fun refresh(req: HttpServletRequest): User {
        val rToken = jwtService.extractJwtFromCookie(req)
        if (rToken.isNullOrBlank()) throw JwtException("")

        val claims = jwtService.extractClaims(rToken)
        if (!claims.getValue(SecurityConstants.TOKEN_TYPE_CLAIM).equals(SecurityConstants.REFRESH_TOKEN_TYPE))
            throw JwtException("")

        val userId = claims.subject
        val user = userService.getById(UUID.fromString(userId))

        setNewJwts(user)

        return user
    }

     fun createRefreshTokenCookie(refreshToken: String): ResponseCookie {
        return ResponseCookie.from(REFRESH_TOKEN_TYPE, refreshToken)
            .httpOnly(true)
            .secure(true)
            .maxAge(jwtProperties.refresh.exp / 1000.toLong())
            .sameSite(COOKIE_SAME_SITE_STRICT)
            .path(COOKIE_PATH)
            .build()
    }

     fun setNewJwts(user: User): User {
        user.accessToken = jwtService.generateAccessToken(user)
        user.refreshToken = jwtService.generateRefreshToken(user)
        return userService.save(user)
    }

    fun setNewAccessToken(user: User): User {
        user.accessToken = jwtService.generateAccessToken(user)
        return userService.save(user)
    }

    fun setNewRefreshToken(user: User): User {
        user.refreshToken = jwtService.generateRefreshToken(user)
        return userService.save(user)
    }


    fun blacklistOldAccessToken(token: String) {}

}
