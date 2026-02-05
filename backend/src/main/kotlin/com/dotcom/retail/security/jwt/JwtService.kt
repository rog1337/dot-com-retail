package com.dotcom.retail.security.jwt

import com.dotcom.retail.common.constants.ApiRoutes.Auth.REFRESH_FULL
import com.dotcom.retail.common.model.TokenType
import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.domain.user.User
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    private val jwtProperties: JwtProperties,
    private val KEY: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(Charsets.UTF_8)),
) {

    companion object {
        private const val BLACKLIST_PREFIX: String = "blacklist:"
        const val TOKEN_TYPE_CLAIM = "type"
        const val BEARER_PREFIX = "Bearer "
        const val BEARER_PREFIX_LENGTH = BEARER_PREFIX.length
    }

    fun generateAccessToken(user: User): String {
        return Jwts
            .builder()
            .claim(TOKEN_TYPE_CLAIM, TokenType.ACCESS.value)
            .subject(user.id.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtProperties.access.exp))
            .signWith(KEY)
            .compact()
    }

    fun generateRefreshToken(user: User): String {
        return Jwts
            .builder()
            .claim(TOKEN_TYPE_CLAIM, TokenType.REFRESH.value)
            .subject(user.id.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtProperties.refresh.exp))
            .signWith(KEY)
            .compact()
    }

    fun extractAndValidateClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(KEY)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun extractJwtFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.firstOrNull { it.name == TokenType.REFRESH.value }?.value
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
        return request.requestURI.equals(REFRESH_FULL) && request.method == HttpMethod.GET.toString()
    }

    // for testing
    fun generateDevToken(): String {
        return Jwts
            .builder()
            .claim(TOKEN_TYPE_CLAIM, TokenType.ACCESS.value)
            .subject("55e62730-36a8-46f9-9d06-e7677254d0fa")
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtProperties.access.exp))
            .signWith(KEY)
            .compact()
    }
}