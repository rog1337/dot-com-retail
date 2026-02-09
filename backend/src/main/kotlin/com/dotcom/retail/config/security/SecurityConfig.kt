package com.dotcom.retail.config.security

import com.dotcom.retail.security.jwt.JwtAuthFilter
import com.dotcom.retail.security.oauth2.OAuth2SuccessHandler
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
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
    private val jwtAuthFilter: JwtAuthFilter,
    private val successHandler: OAuth2SuccessHandler,
    private val authenticationProvider: AuthenticationProvider
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/**")
            .cors {}
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth -> auth
                .requestMatchers(*SecurityMatchers.PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
//                .anyRequest().permitAll()
            }
            .authenticationProvider(authenticationProvider)
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