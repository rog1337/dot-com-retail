package com.dotcom.retail.security.oauth2

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class OAuth2Service(
    @Value("\${frontend.url}") val FRONTEND_URL: String,
) {
    companion object {
        const val OAUTH2_AUTH_FAILED_MSG = "OAuth2 Authentication Failed"
    }

    fun errorRedirect(response: HttpServletResponse, message: String? = OAUTH2_AUTH_FAILED_MSG) {
        response.contentType = MediaType.TEXT_HTML_VALUE
        //language=html
        response.writer.write("""
            <html>
            <head>
                <meta http-equiv="refresh" content="5;url=${FRONTEND_URL}" />
                <script>
                    setTimeout(() => window.location.href = ${FRONTEND_URL}, 5000);
                </script>
            </head>
            <body>
                <h1>${message}</h1>
                Redirecting in 5 seconds...
            </body>
            </html>
        """.trimIndent()
        )
        response.writer.flush()
    }
}