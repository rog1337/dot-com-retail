package com.dotcom.retail.config.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

@ConfigurationProperties(prefix = "password")
data class PasswordProperties(
    val reset: PasswordResetExpiration
)


class PasswordResetExpiration(
    val duration: Duration
)
