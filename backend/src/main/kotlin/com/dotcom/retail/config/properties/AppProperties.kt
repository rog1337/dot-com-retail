package com.dotcom.retail.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val internalUrl: String,
    val publicUrl: String
)