package com.dotcom.retail.security.ratelimit

import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.BucketProxy
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitService(
    private val proxyManager: ProxyManager<String>,
    private val config: BucketConfiguration,
) {

    companion object {
        const val RATE_LIMIT_PREFIX = "rate_limit:"
    }

    private val buckets = ConcurrentHashMap<String, BucketProxy>()

    fun resolveBucket(key: String): BucketProxy {
        val redisKey = (RATE_LIMIT_PREFIX + key)
        return buckets.computeIfAbsent(key) {
            proxyManager.builder().build(redisKey) { config }
        }
    }
}