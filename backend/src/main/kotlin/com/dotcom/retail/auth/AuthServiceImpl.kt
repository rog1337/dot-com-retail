package com.dotcom.retail.auth

import com.dotcom.retail.security.JwtService
import com.dotcom.retail.security.SecurityConstants.COOKIE_PATH
import com.dotcom.retail.security.SecurityConstants.COOKIE_SAME_SITE_STRICT
import com.dotcom.retail.security.SecurityConstants.REFRESH_TOKEN_EXPIRATION_MS
import com.dotcom.retail.security.SecurityConstants.REFRESH_TOKEN_TYPE
import com.dotcom.retail.user.User
import com.dotcom.retail.user.UserService
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val userService: UserService,
    private val jwtService: JwtService,
) : AuthService {

    override fun register(request: RegisterRequest): User {
        val user = userService.create(request)
        setUserAccessToken(user)
        setUserRefreshToken(user)

        return user
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
