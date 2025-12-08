package com.dotcom.retail.unit.jwt

import com.dotcom.retail.common.constants.SecurityConstants
import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.security.jwt.JwtService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.util.Date
import java.util.UUID
import kotlin.test.*

@SpringBootTest
//@TestPropertySource(locations = ["classpath:application-test.properties"])
@ActiveProfiles("test")
class JwtServiceTest(@Autowired private val jwtProperties: JwtProperties) {

    @Autowired
    private lateinit var jwtService: JwtService

    private fun createTestUser() = User(
        id = UUID.randomUUID(),
        email = "test@email.com",
        displayName = "test",
    )

    @Test
    fun `should generate valid access token`() {
        val user = createTestUser()
        val token = jwtService.generateAccessToken(user)

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertDoesNotThrow { jwtService.extractClaims(token) }
    }

    @Test
    fun `should generate valid refresh token`() {
        val userDetails = createTestUser()
        val token = jwtService.generateRefreshToken(userDetails)

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertDoesNotThrow { jwtService.extractClaims(token) }
    }

    @Test
    fun `should reject invalid token`() {
        val invalidJwt = "invalid.jwt"

        assertThrows<Exception> {
            jwtService.extractClaims(invalidJwt)
        }
    }

    @Test
    fun `should reject token with invalid key`() {
        val invalidJwtSecret = "invalidJwtSecret42371894723847283472839"
        val user = createTestUser()
        val tamperedToken = Jwts
            .builder()
            .claim(SecurityConstants.TOKEN_TYPE_CLAIM, SecurityConstants.ACCESS_TOKEN_TYPE)
            .subject(user.id.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtProperties.refresh.exp))
            .signWith(Keys.hmacShaKeyFor(invalidJwtSecret.toByteArray(Charsets.UTF_8)))
            .compact()

        assertThrows<Exception> {
            jwtService.extractClaims(tamperedToken)
        }
    }
}