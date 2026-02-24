package com.dotcom.retail.config.properties

import com.stripe.Stripe
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stripe")
data class StripeProperties(
    val apiKey: String,
    val webhookSecret: String
) {

    @PostConstruct
    fun init() {
        Stripe.apiKey = apiKey
    }
}