package com.dotcom.retail.config

import com.dotcom.retail.security.jwt.JwtAuthFilter
import com.dotcom.retail.security.oauth2.OAuth2SuccessHandler
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val passwordEncoder: PasswordEncoder,
    private val jwtAuthFilter: JwtAuthFilter,
    private val userDetailsService: UserDetailsService,
    private val successHandler: OAuth2SuccessHandler
) {

    companion object {
        const val API_URI = "/api"
//        private val PUBLIC_ENDPOINTS: List<String> = listOf("api/v1/auth/register", "/api/v1/auth/login")
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, restTemplateBuilder: RestTemplateBuilder): SecurityFilterChain {
        http
            .securityMatcher("/**")
            .cors {}
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.ALWAYS) }
            .authorizeHttpRequests { auth -> auth
//                .anyRequest().authenticated()
                .anyRequest().permitAll()
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .oauth2Login { conf -> conf
                .authorizationEndpoint { it.baseUri("/api/v1/oauth2/authorize") }
                .redirectionEndpoint { it.baseUri("/api/v1/oauth2/code/*") }
                .successHandler(successHandler)
                .failureHandler { _, response, _ -> println("failureHandler: $response") }

            }
            .exceptionHandling { e -> e
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val cfg = CorsConfiguration()
        cfg.allowedOrigins = listOf("http://localhost:3000")
//        cfg.allowedOrigins = listOf("*")
        cfg.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        cfg.allowedHeaders = listOf("*")
        cfg.allowCredentials = true
        cfg.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", cfg)
        return source
    }

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.getAuthenticationManager()

    @Bean
    fun authenticationEntryPoint() = AuthenticationEntryPoint { _, response, _ ->
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(
            ObjectMapper().writeValueAsString(
                mapOf(
                    "status" to HttpServletResponse.SC_UNAUTHORIZED,
                    "error" to "Unauthorized",
                )
            )
        )
    }

    @Bean
    fun accessDeniedHandler() = AccessDeniedHandler { _, response, _ ->
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(
            ObjectMapper().writeValueAsString(
                mapOf(
                    "status" to HttpServletResponse.SC_FORBIDDEN,
                    "error" to "Forbidden",
                )
            )
        )
    }


}