package com.dotcom.retail.auth

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.common.constants.ApiRoutes.Auth
import com.dotcom.retail.common.model.TokenType
import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.domain.auth.AuthService
import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.PasswordResetRequest
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserRepository
import com.dotcom.retail.security.jwt.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @MockkBean
    private lateinit var redisTemplate: StringRedisTemplate

    @MockkBean
    private lateinit var valueOperations: ValueOperations<String, String>

    @SpykBean
    private lateinit var authService: AuthService

    @MockkBean(relaxed = true)
    private lateinit var emailService: com.dotcom.retail.common.service.EmailService

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.set(any(), any(), any<Duration>()) } just runs
        every { valueOperations.set(any(), any()) } just runs
        every { valueOperations.get(any()) } returns null
        every { authService.verifyCaptcha(any()) } returns true
    }

    @Test
    fun `register should persist user and return tokens when request is valid`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "StrongPassword123!",
            displayName = "Test User",
            captchaToken = "valid-token"
        )

        mockMvc.post(Auth.BASE + Auth.REGISTER) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            cookie { exists(TokenType.REFRESH) }
            jsonPath("$.accessToken") { isNotEmpty() }
            jsonPath("$.user.email") { value(request.email) }
        }

        val user = userRepository.findByEmail(request.email)
        assertNotNull(user)
        assertEquals(request.displayName, user!!.displayName)
        assertTrue(passwordEncoder.matches(request.password, user.password))
    }

    @Test
    fun `register should fail when captcha is invalid`() {
        every { authService.verifyCaptcha(any()) } returns false

        val request = RegisterRequest("bot@example.com", "password", "bot", "bad-token")

        mockMvc.post(Auth.BASE + Auth.REGISTER) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isUnauthorized() }
        }

        assertNull(userRepository.findByEmail("bot@example.com"))
    }

    @Test
    fun `login should return tokens and httpOnly cookie when credentials are valid`() {
        createTestUser("login@example.com", "password")

        val loginRequest = LoginRequest("login@example.com", "password", null)

        mockMvc.post(Auth.BASE + Auth.LOGIN) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
            cookie {
                exists(TokenType.REFRESH)
                httpOnly(TokenType.REFRESH, true)
            }
            jsonPath("$.accessToken") { isNotEmpty() }
        }
    }

    @Test
    fun `login should return 202 Accepted when 2FA is enabled`() {
        val user = createTestUser("2fa@example.com", "password")
        user.twoFactorEnabled = true
        user.twoFactorSecret = "SECRET"
        userRepository.save(user)

        val loginRequest = LoginRequest("2fa@example.com", "password", null)

        mockMvc.post(Auth.BASE + Auth.LOGIN) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isAccepted() }
            jsonPath("$") { doesNotExist() }
        }
    }

    @Test
    fun `refresh should issue new tokens when cookie is valid`() {
        val user = createTestUser("refresh@example.com", "password")
        val tokenPair = jwtService.rotateTokens(user.id)

        val refreshCookie = Cookie(TokenType.REFRESH, tokenPair.refreshToken)

        val version = jwtService.validateTokenAndExtractClaims(tokenPair.refreshToken)["ver"]
        every { valueOperations.get(any()) } returns version.toString()

        mockMvc.get(Auth.BASE + Auth.REFRESH) {
            cookie(refreshCookie)
        }.andExpect {
            status { isOk() }
            cookie { exists(TokenType.REFRESH) }
            jsonPath("$.accessToken") { isNotEmpty() }
        }
    }

    @Test
    fun `refresh should fail when cookie is missing`() {
        mockMvc.get(Auth.BASE + Auth.REFRESH)
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `resetPassword should initiate process and store token in Redis`() {
        createTestUser("reset@example.com", "oldPass")

        val request = PasswordResetRequest("reset@example.com")

        every { valueOperations.set(any<String>(), any<String>(), any<Duration>()) } just runs

        mockMvc.post(Auth.BASE + Auth.RESET_PASSWORD) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isAccepted() }
        }

        verify(exactly = 1) { emailService.sendPasswordReset(eq("reset@example.com"), any()) }
        verify(exactly = 1) { valueOperations.set(any<String>(), eq("reset@example.com"), any<Duration>()) }
    }

    @Test
    fun `setup 2FA should generate secret and QR code`() {
        val user = createTestUser("setup2fa@example.com", "password")
        val accessToken = jwtService.rotateTokens(user.id).accessToken

        val version = jwtService.validateTokenAndExtractClaims(accessToken)["ver"]
        every { valueOperations.get(any()) } returns version.toString()

        mockMvc.post(ApiRoutes.TwoFactorAuth.BASE + ApiRoutes.TwoFactorAuth.SETUP) {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.secret") { isNotEmpty() }
            jsonPath("$.qrCode") { isNotEmpty() }
        }

        val updatedUser = userRepository.findById(user.id).get()
        assertNotNull(updatedUser.twoFactorSecret)
    }

    private fun createTestUser(email: String, pass: String): User {
        val user = User(
            email = email,
            displayName = "Test",
            passwordHash = passwordEncoder.encode(pass)
        )
        return userRepository.save(user)
    }
}