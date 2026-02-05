package com.dotcom.retail.security.jwt

import com.dotcom.retail.common.constants.ApiRoutes.Auth.REFRESH_FULL
import com.dotcom.retail.common.model.TokenType
import com.dotcom.retail.config.properties.JwtProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    private val jwtProperties: JwtProperties,
    private val KEY: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(Charsets.UTF_8)),
    private val redisTemplate: StringRedisTemplate
) {

    companion object {
        private const val BLACKLIST_PREFIX: String = "blacklist:"
        const val TOKEN_TYPE_CLAIM = "type"
        const val BEARER_PREFIX = "Bearer "
        const val BEARER_PREFIX_LENGTH = BEARER_PREFIX.length
    }

    fun blacklistToken(jti: String, expiration: Date) {
        val expirationTime = expiration.time
        val ttlMs = expirationTime - System.currentTimeMillis()

        if (ttlMs > 0) {
            redisTemplate.opsForValue().set("$BLACKLIST_PREFIX$jti", expirationTime.toString(), Duration.ofMillis(ttlMs))
        }
    }

    fun isBlacklisted(jti: String): Boolean {
        return redisTemplate.hasKey("$BLACKLIST_PREFIX$jti") == true
    }

    fun generateAccessToken(userId: UUID): String {
        return Jwts
            .builder()
            .id(UUID.randomUUID().toString())
            .claim(TOKEN_TYPE_CLAIM, TokenType.ACCESS)
            .subject(userId.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtProperties.access.exp))
            .signWith(KEY)
            .compact()
    }

    fun generateRefreshToken(userId: UUID): String {
        return Jwts
            .builder()
            .id(UUID.randomUUID().toString())
            .claim(TOKEN_TYPE_CLAIM, TokenType.REFRESH)
            .subject(userId.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtProperties.refresh.exp))
            .signWith(KEY)
            .compact()
    }

    fun validateTokenAndExtractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(KEY)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun extractBearerToken(header: String): String {
        return header.substring(BEARER_PREFIX_LENGTH).trim()
    }

    fun extractJwtFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.firstOrNull { it.name == TokenType.REFRESH }?.value
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

    // for testing
    fun generateDevToken(): String {
        return Jwts
            .builder()
            .id(UUID.randomUUID().toString())
            .claim(TOKEN_TYPE_CLAIM, TokenType.ACCESS)
            .subject("55e62730-36a8-46f9-9d06-e7677254d0fa")
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtProperties.access.exp))
            .signWith(KEY)
            .compact()
    }
}