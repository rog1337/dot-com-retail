package com.dotcom.retail.security.jwt

import com.dotcom.retail.common.constants.SecurityConstants
import com.dotcom.retail.config.security.SecurityMatchers
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.lang.NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

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
            val generatedToken = jwtService.generateDevToken()
            println("generated token: \n$generatedToken")

            val authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER)
            if (authHeader.isNullOrBlank() || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
                filterChain.doFilter(request, response)
                return
            }

            val token = authHeader.substring(SecurityConstants.BEARER_TOKEN_START_INDEX)
            if (token.isBlank()) throw Exception()

            val claims = jwtService.extractClaims(token)

            if (!claims.getValue(SecurityConstants.TOKEN_TYPE_CLAIM).equals(SecurityConstants.ACCESS_TOKEN_TYPE))
                throw Exception()

            val id = claims.subject
            if (id.isNullOrBlank()) throw Exception()

            val authToken = UsernamePasswordAuthenticationToken(id, null, null)
            authToken.details = WebAuthenticationDetails(request)
            SecurityContextHolder.getContext().authentication = authToken

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            resolver.resolveException(request, response, null, e)
        }
    }

 }