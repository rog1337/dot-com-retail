package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.constants.ApiRoutes.Auth
import com.dotcom.retail.domain.auth.dto.AuthResponse
import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.user.toDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Auth.BASE)
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping(Auth.REGISTER)
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<Any> {
        val user = authService.register(registerRequest)
        val cookie = authService.createRefreshTokenCookie(user.refreshToken.toString())

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(AuthService.COOKIE_HEADER_NAME, cookie.toString())
            .body(AuthResponse(user.accessToken.toString(), user.toDto()))
    }

    @PostMapping(Auth.LOGIN)
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        val user = authService.login(loginRequest)
        val cookie = authService.createRefreshTokenCookie(user.refreshToken.toString())

        return ResponseEntity
            .ok()
            .header(AuthService.COOKIE_HEADER_NAME, cookie.toString())
            .body(AuthResponse(user.accessToken.toString(), user.toDto()))
    }

    @GetMapping(Auth.REFRESH)
    fun refresh(req: HttpServletRequest): ResponseEntity<AuthResponse> {
        val user = authService.refresh(req)
        val cookie = authService.createRefreshTokenCookie(user.refreshToken.toString())
        return ResponseEntity
            .ok()
            .header(AuthService.COOKIE_HEADER_NAME, cookie.toString())
            .body(AuthResponse(user.accessToken.toString(), user.toDto()))
    }

}