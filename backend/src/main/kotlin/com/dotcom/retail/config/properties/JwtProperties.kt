package com.dotcom.retail.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val access: TokenProperties,
    val refresh: TokenProperties,
    val secret: String
) {
    data class TokenProperties (
        val exp: Long
    )
}