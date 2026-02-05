package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.constants.ApiRoutes.Auth
import com.dotcom.retail.common.model.TokenType
import com.dotcom.retail.domain.auth.dto.AuthResponse
import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.RefreshResponse
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.user.toDto
import com.dotcom.retail.security.jwt.JwtService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Auth.BASE)
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService,
) {

    @PostMapping(Auth.REGISTER)
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<Any> {
        val user = authService.register(registerRequest)
        val refreshToken = jwtService.generateRefreshToken(user.id)
        val accessToken = jwtService.generateAccessToken(user.id)
        val cookie = authService.createRefreshTokenCookie(refreshToken)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(AuthService.COOKIE_HEADER_NAME, cookie.toString())
            .body(AuthResponse(accessToken, user.toDto()))
    }

    @PostMapping(Auth.LOGIN)
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        val user = authService.login(loginRequest)
        val accessToken = jwtService.generateAccessToken(user.id)
        val refreshToken = jwtService.generateRefreshToken(user.id)
        val cookie = authService.createRefreshTokenCookie(refreshToken)

        return ResponseEntity
            .ok()
            .header(AuthService.COOKIE_HEADER_NAME, cookie.toString())
            .body(AuthResponse(accessToken, user.toDto()))
    }

    @GetMapping(Auth.REFRESH)
    fun refresh(request: HttpServletRequest): ResponseEntity<RefreshResponse> {
        val userId = authService.refresh(request)
        val accessToken = jwtService.generateAccessToken(userId)
        val refreshToken = jwtService.generateRefreshToken(userId)
        val cookie = authService.createRefreshTokenCookie(refreshToken)

        return ResponseEntity
            .ok()
            .header(AuthService.COOKIE_HEADER_NAME, cookie.toString())
            .body(RefreshResponse(accessToken))
    }

}