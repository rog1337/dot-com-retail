package com.dotcom.retail.config.ratelimit

import com.dotcom.retail.config.properties.RateLimitProperties
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce
import io.lettuce.core.RedisClient
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class RateLimitConfig(
    private val rateLimitProperties: RateLimitProperties,
) {

    @Bean
    fun proxyManager(connectionFactory: LettuceConnectionFactory): ProxyManager<String> {

        val redisClient = connectionFactory.nativeClient as RedisClient

        val connection = redisClient.connect(
            RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        )

        return Bucket4jLettuce.casBasedBuilder(connection)
            .expirationAfterWrite(
                ExpirationAfterWriteStrategy
                    .basedOnTimeForRefillingBucketUpToMax(rateLimitProperties.bucketExpiration))
            .build()
    }

    @Bean
    fun bucketConfiguration(): BucketConfiguration {
        val limit = Bandwidth.builder()
            .capacity(rateLimitProperties.capacity)
            .refillGreedy(rateLimitProperties.refill, rateLimitProperties.refillInterval)
            .build()

        return BucketConfiguration.builder()
            .addLimit(limit)
            .build()
    }
}