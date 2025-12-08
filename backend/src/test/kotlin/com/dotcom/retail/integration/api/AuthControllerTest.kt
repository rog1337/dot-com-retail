package com.dotcom.retail.integration.api

import com.dotcom.retail.domain.auth.AuthService
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.user.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

//@WebMvcTest(AuthController::class)
//@AutoConfigureMockMvc(addFilters = false)
//class AuthControllerTest(
//    @Autowired val mockMvc: MockMvc
//) {
//
//    @MockkBean
//    private lateinit var authService: AuthService
//    @MockkBean
//    private lateinit var jwtService: JwtService
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper
//    @MockkBean
//    private lateinit var exceptionService: ExceptionService
//    @MockkBean
//    private lateinit var passwordEncoder: PasswordEncoder
//
//    @Test
//    fun `login should return access token and user details`() {
//
//        val loginRequest = LoginRequest(
//            email = "testemail@email.com",
//            password = "password",
//        )
//
//        val login = mockMvc.perform(
//            MockMvcRequestBuilders.post(AuthController.Companion.AUTH_BASE_PATH + AuthController.Companion.LOGIN_PATH)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(loginRequest))
//        )
//            .andDo { println(it.response.contentAsString) }
//            .andExpect(MockMvcResultMatchers.status().isOk)
//    }
//}

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerTest(@Autowired private val userRepository: UserRepository) {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

//    @MockitoBean
    @MockitoSpyBean
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        whenever(authService.verifyCaptcha("testCaptchaToken")).thenReturn(true)
    }

    @Test
    fun `should register new user successfully`() {
        val registerRequest = RegisterRequest(
            email = "newuser@test.com",
            password = "password",
            confirmPassword = "password",
            displayName = "Test",
            captchaToken = "testCaptchaToken"
        )

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andDo { println("response" + it.response.contentAsString) }
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.user.email").value("newuser@test.com"))
            .andExpect(jsonPath("$.accessToken").exists())


        assertNotNull(userRepository.findByEmail(registerRequest.email))
        println(userRepository.findAll())
    }

}
