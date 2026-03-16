package com.dotcom.retail.config.properties

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
    lateinit var frontendProperties: FrontendProperties

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        passwordResetUrl = frontendProperties.passwordResetUrl
    }

    lateinit var passwordResetUrl: String
}


class PasswordResetExpiration(
    val duration: Duration
)
