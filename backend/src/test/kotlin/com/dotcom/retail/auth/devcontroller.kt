package com.dotcom.retail.auth

import com.dotcom.retail.common.exception.ExceptionService
import com.dotcom.retail.dev.controller
import com.dotcom.retail.domain.auth.AuthController
import com.dotcom.retail.domain.auth.AuthService
import com.dotcom.retail.security.jwt.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(
    controllers = [controller::class],
//    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
@AutoConfigureMockMvc(addFilters = false)
//@WithMockUser(username = "test@email.com", roles = ["USER"])
class devcontroller(
    @Autowired val mockMvc: MockMvc
) {

    @MockkBean
    private lateinit var authService: AuthService
    @MockkBean
    private lateinit var jwtService: JwtService
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @MockkBean
    private lateinit var exceptionService: ExceptionService
    @MockkBean
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun home() {
        mockMvc.perform(
            get("/")
        )
            .andDo { println(it.response.contentAsString)}
    }

}