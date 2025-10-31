package com.dotcom.retail.domain.auth

import com.dotcom.retail.security.jwt.JwtService
import com.dotcom.retail.common.constants.SecurityConstants.COOKIE_PATH
import com.dotcom.retail.common.constants.SecurityConstants.COOKIE_SAME_SITE_STRICT
import com.dotcom.retail.common.constants.SecurityConstants.REFRESH_TOKEN_EXPIRATION_MS
import com.dotcom.retail.common.constants.SecurityConstants.REFRESH_TOKEN_TYPE
import com.dotcom.retail.domain.auth.dto.AuthResponse
import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.RegisterOAuthUser
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.user.CreateUserParams
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserService
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val userService: UserService,
    private val jwtService: JwtService,
) : AuthService {

    override fun register(request: RegisterRequest): User {
        val user = userService.create(CreateUserParams(
            request.email,
            request.displayName,
            request.password
        ))
        setUserAuthenticationTokens(user)

        return user
    }

    override fun registerOAuthUser(details: RegisterOAuthUser): User {
        val user = User(
            email = details.email,
            displayName = details.displayName,
        )

        return userService.save(user)
    }

    override fun login(request: LoginRequest): AuthResponse {
        TODO("Not yet implemented")
    }

    override fun createRefreshTokenCookie(refreshToken: String): ResponseCookie {
        return ResponseCookie.from(REFRESH_TOKEN_TYPE, refreshToken)
            .httpOnly(true)
            .secure(true)
            .maxAge(REFRESH_TOKEN_EXPIRATION_MS / 1000.toLong())
            .sameSite(COOKIE_SAME_SITE_STRICT)
            .path(COOKIE_PATH)
            .build()
    }

    override fun setUserAuthenticationTokens(user: User): User {
        setUserAccessToken(user)
        return setUserRefreshToken(user)
    }

    fun setUserAccessToken(user: User): User {
        user.accessToken = jwtService.generateAccessToken(user)
        return userService.save(user)
    }

    fun setUserRefreshToken(user: User): User {
        user.refreshToken = jwtService.generateRefreshToken(user)
        return userService.save(user)
    }


    fun blacklistOldAccessToken(token: String) {}

}
