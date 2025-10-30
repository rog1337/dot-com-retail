package com.dotcom.retail.security.jwt

import com.dotcom.retail.security.SecurityConstants
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.lang.NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {

    companion object {
        const val INVALID_REFRESH_TOKEN_MSG = "Invalid refresh token"
        const val INVALID_ACCESS_TOKEN_MSG = "Invalid access token"
    }

    override fun doFilterInternal(
        @NonNull request: HttpServletRequest,
        @NonNull response: HttpServletResponse,
        @NonNull filterChain: FilterChain
    ) {

        try {
            val generatedToken = jwtService.generateDevToken()
            println("generated token: \n$generatedToken")

            val claims: Claims?

            if (jwtService.isRefreshRequest(request)) {

                val token = jwtService.extractJwtFromCookie(request)
                if (token.isNullOrBlank()) throw Exception(INVALID_REFRESH_TOKEN_MSG)

                claims = jwtService.extractClaims(token)

                if (!claims.getValue(SecurityConstants.TOKEN_TYPE_CLAIM).equals(SecurityConstants.REFRESH_TOKEN_TYPE))
                    throw Exception(INVALID_REFRESH_TOKEN_MSG)

            } else {
                val authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER)

                if (authHeader.isNullOrBlank() || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
                    filterChain.doFilter(request, response)
                    return
                }

                val token = authHeader.substring(SecurityConstants.BEARER_TOKEN_START_INDEX)
                if (token.isBlank()) throw Exception(INVALID_REFRESH_TOKEN_MSG)

                claims = jwtService.extractClaims(token)

                if (!claims.getValue(SecurityConstants.TOKEN_TYPE_CLAIM).equals(SecurityConstants.ACCESS_TOKEN_TYPE))
                    throw Exception(INVALID_ACCESS_TOKEN_MSG)
            }

            val id = claims.subject
            if (id.isNullOrBlank()) throw Exception(INVALID_ACCESS_TOKEN_MSG)

            val authToken = UsernamePasswordAuthenticationToken(id, null, null)
            authToken.details = WebAuthenticationDetails(request)
            SecurityContextHolder.getContext().authentication = authToken

            filterChain.doFilter(request, response)

        } catch (e: ExpiredJwtException) {
            jwtService.sendResponse(response, HttpStatus.UNAUTHORIZED, "Authentication token expired")
        } catch (e: SignatureException) {
            jwtService.sendResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT signature")
        } catch (e: UsernameNotFoundException) {
            jwtService.sendResponse(response, HttpStatus.UNAUTHORIZED, e.message ?: "User not found")
        } catch (e: UnsupportedJwtException) {
            jwtService.sendResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT")
        } catch (e: Exception) {
            e.printStackTrace()

            jwtService.sendResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Token authentication failure",
                "Possibly invalid JWT token"
            )
        }
    }

 }