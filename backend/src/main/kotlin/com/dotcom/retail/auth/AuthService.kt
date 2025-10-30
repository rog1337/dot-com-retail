package com.dotcom.retail.auth

import com.dotcom.retail.user.User
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

interface AuthService {

    fun register(request: RegisterRequest): User

    fun login(request: LoginRequest): AuthResponse

    fun createRefreshTokenCookie(refreshToken: String): ResponseCookie

    fun registerOAuthUser(details: RegisterOAuthUser): User

    fun setUserAuthenticationTokens(user: User): User

}