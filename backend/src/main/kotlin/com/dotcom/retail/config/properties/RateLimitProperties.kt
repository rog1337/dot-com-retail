package com.dotcom.retail.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "ratelimit")
data class RateLimitProperties (
    val capacity: Long,
    val refill: Long,
    val refillInterval: Duration,
    val bucketExpiration: Duration,
)