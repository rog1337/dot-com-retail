package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.annotation.RequiresTwoFactorAuth
import com.dotcom.retail.common.constants.ApiRoutes.Auth
import com.dotcom.retail.domain.auth.dto.RegisterResponse
import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.LoginResponse
import com.dotcom.retail.domain.auth.dto.LoginResult
import com.dotcom.retail.domain.auth.dto.PasswordResetRequest
import com.dotcom.retail.domain.auth.dto.PasswordResetVerification
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(Auth.BASE)
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService,
    private val passwordResetService: PasswordResetService,
) {

    @PostMapping(Auth.REGISTER)
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<RegisterResponse> {
        val user = authService.register(registerRequest)
        val tokenPair = jwtService.rotateTokens(user.id)
        val cookie = authService.createRefreshTokenCookie(tokenPair.refreshToken)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(RegisterResponse(tokenPair.accessToken, user.toDto()))
    }

    @PostMapping(Auth.LOGIN)
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        return when (val loginResult = authService.login(loginRequest)) {
            is LoginResult.Success -> {
                val cookie = authService.createRefreshTokenCookie(loginResult.refreshToken)
                ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(LoginResponse(loginResult.accessToken, loginResult.user))
            }

            is LoginResult.TwoFactorRequired -> {
                ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .build()
            }
        }
    }

    @GetMapping(Auth.REFRESH)
    fun refresh(request: HttpServletRequest): ResponseEntity<RefreshResponse> {
        val tokenPair = authService.refreshTokens(request)
        val cookie = authService.createRefreshTokenCookie(tokenPair.refreshToken)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(RefreshResponse(tokenPair.accessToken))
    }

    @RequiresTwoFactorAuth
    @GetMapping(Auth.LOGOUT)
    fun logout(@AuthenticationPrincipal userId: UUID): ResponseEntity<Void> {
        authService.logout(userId)
        val cookie = authService.removeRefreshTokenCookie()
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build()
    }

    @PostMapping(Auth.RESET_PASSWORD)
    fun resetPassword(@RequestBody request: PasswordResetRequest): ResponseEntity<Void> {
        passwordResetService.initiatePasswordReset(request)
        return ResponseEntity.accepted().build()
    }

    @PostMapping(Auth.RESET_PASSWORD_VERIFY)
    fun resetPasswordVerify(@RequestBody data: PasswordResetVerification): ResponseEntity<Void> {
        passwordResetService.resetPassword(data)
        return ResponseEntity.noContent().build()
    }

}