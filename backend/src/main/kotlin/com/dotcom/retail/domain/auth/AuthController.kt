package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.constants.ApiConstants.V1
import com.dotcom.retail.common.constants.SecurityConstants.COOKIE_HEADER_NAME
import com.dotcom.retail.domain.auth.AuthController.Companion.AUTH_BASE_PATH
import com.dotcom.retail.domain.auth.dto.AuthResponse
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.user.UserService
import com.dotcom.retail.domain.user.toDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AUTH_BASE_PATH)
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
) {

    companion object {
        const val AUTH_BASE_PATH = "$V1/auth"
        const val REGISTER_PATH = "/register"
        const val LOGIN_PATH = "/login"
        const val REFRESH_PATH = "/refresh"
        const val REFRESH_PATH_FULL = "$AUTH_BASE_PATH$REFRESH_PATH"
    }

    @PostMapping(REGISTER_PATH)
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<Any> {
        val user = authService.register(registerRequest)
        val cookie = authService.createRefreshTokenCookie(user.refreshToken.toString())

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(COOKIE_HEADER_NAME, cookie.toString())
            .body(AuthResponse(user.accessToken.toString(), user.toDto()))
    }

    @GetMapping("/cookie")
    fun getCookie(response: HttpServletResponse, @AuthenticationPrincipal id: String): ResponseEntity<Void> {
        println("controller user: " + id)
        val value = "testCookie123"

        val cookie: ResponseCookie = ResponseCookie.from("refresh-token", value)
            .httpOnly(true)
            .secure(true)
            .maxAge(3600)
            .sameSite("Lax")
            .path("/")
            .build()


        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping(REFRESH_PATH)
    fun refresh(request: HttpServletRequest): ResponseEntity<Void> {
        return ResponseEntity.ok().build()
    }

}