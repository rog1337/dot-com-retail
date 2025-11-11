package com.dotcom.retail.security.jwt

import com.dotcom.retail.common.constants.SecurityConstants
import com.dotcom.retail.common.constants.SecurityConstants.REFRESH_TOKEN_TYPE
import com.dotcom.retail.domain.auth.AuthController
import com.dotcom.retail.domain.user.User
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
) {

    private val KEY = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    fun generateAccessToken(user: User): String {
        return Jwts
            .builder()
            .claim(SecurityConstants.TOKEN_TYPE_CLAIM, SecurityConstants.ACCESS_TOKEN_TYPE)
            .subject(user.id.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + SecurityConstants.ACCESS_TOKEN_EXPIRATION_MS))
            .signWith(KEY)
            .compact()
    }

    fun generateRefreshToken(user: User): String {
        return Jwts
            .builder()
            .claim(SecurityConstants.TOKEN_TYPE_CLAIM, SecurityConstants.REFRESH_TOKEN_TYPE)
            .subject(user.id.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + SecurityConstants.REFRESH_TOKEN_EXPIRATION_MS))
            .signWith(KEY)
            .compact()
    }

    fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(KEY)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun extractJwtFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.firstOrNull { it.name == REFRESH_TOKEN_TYPE }?.value
    }

    fun sendResponse(response: HttpServletResponse, status: HttpStatus, message: String? = null, details: String? = null) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = status.value()

        val body = mapOf(
            "status" to status.value(),
            "message" to message,
            "details" to details
        )

        ObjectMapper().writeValue(response.outputStream, body)
    }

    fun isRefreshRequest(request: HttpServletRequest): Boolean {
        return request.requestURI.equals(AuthController.REFRESH_PATH_FULL) && request.method == HttpMethod.GET.toString()
    }

    // for testing
    fun generateDevToken(): String {
        return Jwts
            .builder()
            .claim(SecurityConstants.TOKEN_TYPE_CLAIM, SecurityConstants.ACCESS_TOKEN_TYPE)
            .subject("55e62730-36a8-46f9-9d06-e7677254d0fa")
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + SecurityConstants.ACCESS_TOKEN_EXPIRATION_MS))
            .signWith(KEY)
            .compact()
    }
}