package com.dotcom.retail.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "frontend")
data class FrontendProperties(
    val internalUrl: String,
    val publicUrl: String,
    val passwordResetUrl: String,
    val accountUrl: String,
)
