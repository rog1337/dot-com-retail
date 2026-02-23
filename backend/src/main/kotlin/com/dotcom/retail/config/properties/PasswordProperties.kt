package com.dotcom.retail.config.properties

import com.dotcom.retail.common.constants.ApiRoutes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.event.EventListener
import java.time.Duration


@ConfigurationProperties(prefix = "password")
data class PasswordProperties(
    val reset: PasswordResetExpiration,
) {
    @Autowired
    lateinit var appProperties: AppProperties

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        passwordResetUrl = "${appProperties.url}/${ApiRoutes.Auth.BASE}${ApiRoutes.Auth.RESET_PASSWORD_VERIFY}"
    }

    lateinit var passwordResetUrl: String
}


class PasswordResetExpiration(
    val duration: Duration
)
