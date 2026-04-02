package com.dotcom.retail.security.ratelimit

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
import java.time.Duration

@Configuration
class RateLimitConfig {

    @Bean
    fun proxyManager(connectionFactory: LettuceConnectionFactory): ProxyManager<String> {

        val redisClient = connectionFactory.nativeClient as RedisClient

        val connection = redisClient.connect(
            RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        )

        return Bucket4jLettuce.casBasedBuilder(connection)
            .expirationAfterWrite(
                ExpirationAfterWriteStrategy
                    .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10)))
            .build()
    }

    @Bean
    fun bucketConfiguration(): BucketConfiguration {
        val limit = Bandwidth.builder()
            .capacity(100)
            .refillGreedy(100, Duration.ofMinutes(1))
            .build()

        return BucketConfiguration.builder()
            .addLimit(limit)
            .build()
    }
}