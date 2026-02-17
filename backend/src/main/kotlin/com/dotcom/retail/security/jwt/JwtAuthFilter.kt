package com.dotcom.retail.security.jwt

import com.dotcom.retail.common.exception.JwtException
import com.dotcom.retail.config.security.SecurityMatchers
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.lang.NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.util.UUID

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    @Qualifier("handlerExceptionResolver")
    private val resolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return SecurityMatchers.PUBLIC_ENDPOINTS.any { pattern ->
            AntPathMatcher().match(pattern, path)
        }
    }

    override fun doFilterInternal(
        @NonNull request: HttpServletRequest,
        @NonNull response: HttpServletResponse,
        @NonNull filterChain: FilterChain
    ) {

        try {
            val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
            if (authHeader.isNullOrBlank() || !authHeader.startsWith(JwtService.BEARER_PREFIX)) {
                filterChain.doFilter(request, response)
                return
            }

            val token = jwtService.extractBearerToken(authHeader)
            if (token.isBlank()) throw Exception()

            val claims = jwtService.validateTokenAndExtractClaims(token)

            if (!jwtService.isAccessToken(claims.getValue(JwtService.TOKEN_TYPE_CLAIM).toString()))
                throw Exception()

            if (!jwtService.isValidTokenVersion(claims.subject, claims.getValue(JwtService.TOKEN_VERSION_CLAIM).toString()))
                throw JwtException()

            val id = claims.subject
            if (id.isNullOrBlank()) throw Exception()
            val userId = UUID.fromString(id)

            val authToken = UsernamePasswordAuthenticationToken(userId, null, null)
            authToken.details = WebAuthenticationDetails(request)
            SecurityContextHolder.getContext().authentication = authToken

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            resolver.resolveException(request, response, null, e)
        }
    }

 }