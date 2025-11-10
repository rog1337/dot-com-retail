package com.dotcom.retail.security.oauth2

import com.dotcom.retail.domain.auth.AuthService
import com.dotcom.retail.common.exception.auth.OAuth2EmailNotVerifiedException
import com.dotcom.retail.common.exception.auth.OAuth2Exception
import com.dotcom.retail.common.constants.SecurityConstants
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

    companion object {
        const val INVALID_OAUTH_ERROR_MESSAGE = "Invalid OAuth2 Authentication"
        const val UNEXPECTED_OAUTH_ERROR_MESSAGE = "Unexpected OAuth2 Authentication"
    }

    val logger = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val auth = authentication.principal as? OidcUser ?: throw OAuth2Exception(UNEXPECTED_OAUTH_ERROR_MESSAGE)

            val email = auth.email
            var user = userService.findByEmail(email)

            if (user == null) {
                if (!auth.emailVerified) {
                    authentication as? OAuth2AuthenticationToken ?: throw OAuth2Exception(INVALID_OAUTH_ERROR_MESSAGE)
                    val provider = authentication.authorizedClientRegistrationId
                    throw OAuth2EmailNotVerifiedException(provider)
                }

                user = authService.registerOAuthUser(
                    RegisterOAuthUser(
                        email,
                        auth.fullName,
                        auth.picture
                    )
                )
            }

            authService.setUserAuthenticationTokens(user)

            val cookie = authService.createRefreshTokenCookie(user.refreshToken.toString())
            response.addHeader(SecurityConstants.COOKIE_HEADER_NAME, cookie.toString())
            response.sendRedirect(oauth2Service.FRONTEND_URL)

        } catch (e: OAuth2Exception) {
            oauth2Service.errorRedirect(response, e.message.orEmpty())
        } catch (e: OAuth2EmailNotVerifiedException) {
            oauth2Service.errorRedirect(response, e.message.orEmpty())
        } catch (e: Exception) {
            oauth2Service.errorRedirect(response, INVALID_OAUTH_ERROR_MESSAGE)
            logger.error(e.message)
        }
    }

}