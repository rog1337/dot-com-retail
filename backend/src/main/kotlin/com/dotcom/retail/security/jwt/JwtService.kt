package com.dotcom.retail.security.jwt

import com.dotcom.retail.common.model.TokenType
import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.security.jwt.TokenPair
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
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
        private const val USER_PREFIX: String = "u:"
        const val TOKEN_TYPE_CLAIM = "typ"
        const val TOKEN_VERSION_CLAIM = "ver"
        const val BEARER_PREFIX = "Bearer "
        const val BEARER_PREFIX_LENGTH = BEARER_PREFIX.length
    }

    fun revokeTokens(userId: UUID) {
        redisTemplate.delete("$USER_PREFIX$userId")
    }

    fun rotateTokens(userId: UUID): TokenPair {
        val version = updateTokenVersion(userId)
        val refreshToken = generateRefreshToken(userId, version)
        val accessToken = generateAccessToken(userId, version)
        return TokenPair(accessToken, refreshToken)
    }

    fun isValidTokenVersion(userId: String, version: String): Boolean {
        val storedVersion = redisTemplate.opsForValue().get("$USER_PREFIX$userId")
        return storedVersion == version
    }

    fun updateTokenVersion(userId: UUID): String {
        val version = Instant.now().epochSecond.toString()
        redisTemplate.opsForValue().set("$USER_PREFIX$userId", version, Duration.ofMillis(jwtProperties.refresh.exp))
        return version
    }

    fun generateAccessToken(userId: UUID, version: String): String {
        return Jwts
            .builder()
            .claim(TOKEN_TYPE_CLAIM, TokenType.ACCESS)
            .claim(TOKEN_VERSION_CLAIM, version)
            .subject(userId.toString())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.access.exp))
            .signWith(KEY)
            .compact()
    }

    fun generateRefreshToken(userId: UUID, version: String?): String {
        return Jwts
            .builder()
            .claim(TOKEN_TYPE_CLAIM, TokenType.REFRESH)
            .claim(TOKEN_VERSION_CLAIM, version)
            .subject(userId.toString())
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

    fun isRefreshToken(type: String): Boolean {
        return TokenType.REFRESH == type
    }

    fun isAccessToken(type: String): Boolean {
        return TokenType.ACCESS == type
    }

    // for testing
//    fun generateDevToken(): String {
//        return Jwts
//            .builder()
//            .claim(TOKEN_TYPE_CLAIM, TokenType.ACCESS)
//            .claim(TOKEN_VERSION_CLAIM, Instant.now().epochSecond)
//            .subject("55e62730-36a8-46f9-9d06-e7677254d0fa")
//            .expiration(Date(System.currentTimeMillis() + jwtProperties.access.exp))
//            .signWith(KEY)
//            .compact()
//    }
}