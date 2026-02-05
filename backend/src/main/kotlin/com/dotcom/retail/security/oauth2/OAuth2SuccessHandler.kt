package com.dotcom.retail.security.oauth2

import com.dotcom.retail.common.exception.OAuthException
import com.dotcom.retail.domain.auth.AuthService
import com.dotcom.retail.domain.auth.dto.RegisterOAuthUser
import com.dotcom.retail.domain.user.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
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
) : AuthenticationSuccessHandler {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val auth = authentication.principal as? OidcUser ?: throw OAuthException.unknownProvider()

            val email = auth.email
            var user = userService.findByEmail(email)

            if (user == null) {
                if (!auth.emailVerified) {
                    authentication as? OAuth2AuthenticationToken ?: throw OAuthException()
                    val provider = authentication.authorizedClientRegistrationId
                    throw OAuthException.emailNotVerified(provider)
                }

                user = authService.registerOAuthUser(
                    RegisterOAuthUser(
                        email,
                        auth.fullName,
                        auth.picture
                    )
                )
            }

            val cookie = authService.createRefreshTokenCookie(user.refreshToken.toString())
            response.addHeader(AuthService.COOKIE_HEADER_NAME, cookie.toString())
            response.sendRedirect(oauth2Service.FRONTEND_URL)

        } catch (e: OAuthException) {
            oauth2Service.errorRedirect(response, e.message)
        } catch (e: Exception) {
            oauth2Service.errorRedirect(response, "Unexpected error occurred")
            logger.error(e.message)
        }
    }

}