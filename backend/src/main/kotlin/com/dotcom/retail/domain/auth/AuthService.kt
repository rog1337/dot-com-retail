package com.dotcom.retail.domain.auth

import com.dotcom.retail.domain.auth.dto.AuthResponse
import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.RegisterOAuthUser
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.user.User
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseCookie

interface AuthService {

    fun register(request: RegisterRequest): User

    fun login(request: LoginRequest): User

    fun refresh(req: HttpServletRequest): User

    fun createRefreshTokenCookie(refreshToken: String): ResponseCookie

    fun registerOAuthUser(details: RegisterOAuthUser): User

    fun setNewJwts(user: User): User

}