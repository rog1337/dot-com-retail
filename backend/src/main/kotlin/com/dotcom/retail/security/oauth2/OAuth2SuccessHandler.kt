package com.dotcom.retail.security.oauth2

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.OAuthError
import com.dotcom.retail.config.properties.FrontendProperties
import com.dotcom.retail.domain.auth.AuthService
import com.dotcom.retail.domain.auth.dto.RegisterOAuthUser
import com.dotcom.retail.domain.user.UserService
import com.dotcom.retail.security.jwt.JwtService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val oauth2Service: OAuth2Service,
    private val userService: UserService,
    private val authService: AuthService,
    private val jwtService: JwtService,
    private val frontendProperties: FrontendProperties,
) : AuthenticationSuccessHandler {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val auth = authentication.principal as? OidcUser ?: throw AppException(OAuthError.OAUTH_UNKNOWN_PROVIDER)

            val email = auth.email
            var user = userService.findByEmail(email)

            if (user == null) {
                if (!auth.emailVerified) {
                    authentication as? OAuth2AuthenticationToken ?: throw AppException(OAuthError.OAUTH_ERROR)
                    val provider = authentication.authorizedClientRegistrationId
                    throw AppException(OAuthError.OAUTH_EMAIL_NOT_VERIFIED)
                }

                user = authService.registerOAuthUser(
                    RegisterOAuthUser(
                        email,
                        auth.fullName,
                        auth.picture
                    )
                )
            }

            val version = jwtService.updateTokenVersion(user.id)
            val cookie = authService.createRefreshTokenCookie(jwtService.generateRefreshToken(user.id, version))
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())

            response.sendRedirect(frontendProperties.url)
        } catch (e: AppException) {
            oauth2Service.errorRedirect(response, e.message)
        } catch (e: Exception) {
            oauth2Service.errorRedirect(response, "Unexpected error occurred")
            logger.error(e.message)
        }
    }

}