package com.dotcom.retail.security

import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.security.jwt.JwtService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verify
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID

class JwtServiceTest {

    @MockK
    lateinit var redisTemplate: StringRedisTemplate
    @MockK
    lateinit var valueOperations: ValueOperations<String, String>
    @MockK
    lateinit var request: HttpServletRequest

    private lateinit var jwtService: JwtService
    private lateinit var jwtProperties: JwtProperties

    private val testSecret = "VGfucERcmymq3JmsYjDczP5Ntb2O3DM3BjtAg6ouk8p"
    val userId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { redisTemplate.opsForValue() } returns valueOperations

        jwtProperties = JwtProperties(
            secret = testSecret,
            access = JwtProperties.TokenProperties(exp = 3600000), // 1 hour
            refresh = JwtProperties.TokenProperties(exp = 86400000) // 24 hours
        )

        jwtService = JwtService(jwtProperties, redisTemplate = redisTemplate)
    }

    @Test
    fun `generateAccessToken generates valid signed access token`() {
        val version = Instant.now().epochSecond.toString()

        val token = jwtService.generateAccessToken(userId, version)

        val claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(testSecret.toByteArray()))
            .build()
            .parseSignedClaims(token)
            .payload

        assertThat(claims.subject).isEqualTo(userId.toString())
        assertThat(claims["typ"]).isEqualTo("access")
        assertThat(claims["ver"]).isEqualTo(version)
    }

    @Test
    fun `generateRefreshToken generates valid signed refresh token`() {
        val version = Instant.now().epochSecond.toString()

        val token = jwtService.generateRefreshToken(userId, version)

        val claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(testSecret.toByteArray()))
            .build()
            .parseSignedClaims(token)
            .payload

        assertThat(claims.subject).isEqualTo(userId.toString())
        assertThat(claims["typ"]).isEqualTo("refresh")
        assertThat(claims["ver"]).isEqualTo(version)
    }

    @Test
    fun `updateTokenVersion updates token version in Redis`() {
        val key = "u:$userId"
        val capturedVersion = slot<String>()
        val capturedDuration = slot<Duration>()

        every {
            valueOperations.set(key, capture(capturedVersion), capture(capturedDuration))
        } returns Unit

        val version = jwtService.updateTokenVersion(userId)

        assertThat(version).isNotNull
        assertThat(capturedVersion.captured).isEqualTo(version)
        assertThat(capturedDuration.captured).isEqualTo(Duration.ofMillis(jwtProperties.refresh.exp))
    }

    @Test
    fun `isValidTokenVersion correctly validates version`() {
        val validVersion = "1234"
        val invalidVersion = "4321"

        every { valueOperations.get("u:$userId") } returns validVersion

        val withValidVersion = jwtService.isValidTokenVersion(userId.toString(), validVersion)
        assertThat(withValidVersion).isTrue

        val withInvalidVersion = jwtService.isValidTokenVersion(userId.toString(), invalidVersion)
        assertThat(withInvalidVersion).isFalse
    }

    @Test
    fun `revokeTokens should delete key from redis`() {
        val key = "u:$userId"
        every { redisTemplate.delete(key) } returns true

        jwtService.revokeTokens(userId)

        verify(exactly = 1) { redisTemplate.delete(key) }
    }

    @Test
    fun `extractBearerToken extracts token correctly`() {
        val header = "Bearer abc.def.ghi"
        val result = jwtService.extractBearerToken(header)
        assertThat(result).isEqualTo("abc.def.ghi")
    }

    @Test
    fun `extractJwtFromCookie returns correct value when cookie exists`() {
        val expectedToken = "cookie.token.value"
        val cookies = arrayOf(
            Cookie("other_cookie", "dummy"),
            Cookie("refresh", expectedToken)
        )
        every { request.cookies } returns cookies

        val result = jwtService.extractJwtFromCookie(request)

        assertThat(result).isEqualTo(expectedToken)
    }

    @Test
    fun `extractJwtFromCookie returns null when cookie is missing`() {
        every { request.cookies } returns arrayOf(Cookie("other", "value"))

        val result = jwtService.extractJwtFromCookie(request)

        assertThat(result).isNull()
    }

    @Test
    fun `extractJwtFromCookie returns null when cookies are null`() {
        every { request.cookies } returns null

        val result = jwtService.extractJwtFromCookie(request)

        assertThat(result).isNull()
    }

    @Test
    fun `validateTokenAndExtractClaims parses valid token successfully`() {
        val version = "1234"
        val token = jwtService.generateAccessToken(userId, version)

        val claims = jwtService.validateTokenAndExtractClaims(token)

        assertThat(claims.subject).isEqualTo(userId.toString())
        assertThat(claims["ver"]).isEqualTo(version)
    }

    @Test
    fun `validateTokenAndExtractClaims throws exception for invalid signature`() {
        val otherSecret = "different-secret-key-that-does-not-match-12345"
        val otherKey = Keys.hmacShaKeyFor(otherSecret.toByteArray())

        val forgedToken = Jwts.builder()
            .subject(userId.toString())
            .signWith(otherKey)
            .compact()

        assertThrows<SignatureException> {
            jwtService.validateTokenAndExtractClaims(forgedToken)
        }
    }

    @Test
    fun `validateTokenAndExtractClaims throws exception for expired token`() {
        val expiredToken = Jwts.builder()
            .subject(userId.toString())
            .signWith(Keys.hmacShaKeyFor(testSecret.toByteArray()))
            .expiration(Date(System.currentTimeMillis() -1))
            .compact()

        assertThrows<ExpiredJwtException> {
            jwtService.validateTokenAndExtractClaims(expiredToken)
        }
    }

}