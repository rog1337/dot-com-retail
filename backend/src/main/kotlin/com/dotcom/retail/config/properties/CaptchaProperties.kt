package com.dotcom.retail.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloudflare.turnstile")
data class TurnstileProperties(
    val verifyUrl: String,
    val secretKey: String,
)