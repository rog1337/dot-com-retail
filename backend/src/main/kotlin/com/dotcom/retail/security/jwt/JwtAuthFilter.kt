package com.dotcom.retail.security.jwt

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.JwtError
import com.dotcom.retail.config.security.SecurityMatchers
import com.dotcom.retail.domain.user.Role
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
import java.util.*

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

    private fun isOptionalAuth(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return SecurityMatchers.OPTIONAL_AUTH_ENDPOINTS.any { AntPathMatcher().match(it, path) }
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
                throw AppException(JwtError.JWT_ACCESS_REVOKED)

            val id = claims.subject
            if (id.isNullOrBlank()) throw Exception()
            val userId = UUID.fromString(id)
            val role = claims[JwtService.TOKEN_ROLE_CLAIM].toString()
            val authorities = listOf(Role.valueOf(role))

            val authToken = UsernamePasswordAuthenticationToken(userId, null, authorities)
            authToken.details = WebAuthenticationDetails(request)
            SecurityContextHolder.getContext().authentication = authToken

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            if (isOptionalAuth(request)) {
                /*
                For endpoints that accept authentication, but don't require it
                 */
                SecurityContextHolder.clearContext()
                filterChain.doFilter(request, response)
            } else {
                resolver.resolveException(request, response, null, e)
            }
        }
    }

}