package com.dotcom.retail.security

import com.dotcom.retail.common.BaseIntegrationTest
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.security.ratelimit.RateLimitService
import com.ninjasquad.springmockk.SpykBean
import io.github.bucket4j.ConsumptionProbe
import io.github.bucket4j.distributed.BucketProxy
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitFilterTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @SpykBean
    private lateinit var rateLimitService: RateLimitService

    private fun allowedBucket(): BucketProxy = mockk {
        every { tryConsumeAndReturnRemaining(1) } returns ConsumptionProbe.consumed(99, 0)
    }

    private fun blockedBucket(): BucketProxy = mockk {
        every { tryConsumeAndReturnRemaining(1) } returns ConsumptionProbe.rejected(0, 1_000_000_000L, 0)
    }

    @Test
    fun `request is allowed when rate limit is not exceeded`() {
        every { rateLimitService.resolveBucket(any()) } returns allowedBucket()

        mockMvc.perform(
            get(ApiRoutes.Product.BASE)
                .with { it.remoteAddr = "10.0.0.1"; it }
        ).andExpect(status().isOk)
    }

    @Test
    fun `request is blocked with 429 when rate limit is exceeded`() {
        every { rateLimitService.resolveBucket(any()) } returns blockedBucket()

        mockMvc.perform(
            get(ApiRoutes.Product.BASE)
                .with { it.remoteAddr = "10.0.0.2"; it }
        ).andExpect(status().isTooManyRequests)
    }

    @Test
    fun `auth endpoint is rate limited independently`() {
        every { rateLimitService.resolveBucket(any()) } returns blockedBucket()

        mockMvc.perform(
            post(ApiRoutes.Auth.BASE + ApiRoutes.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"x@x.com","password":"wrong"}""")
                .with { it.remoteAddr = "10.0.0.3"; it }
        ).andExpect(status().isTooManyRequests)
    }

    @Test
    fun `different IPs are rate limited independently`() {
        every { rateLimitService.resolveBucket("10.0.0.50") } returns allowedBucket()
        every { rateLimitService.resolveBucket("10.0.0.99") } returns blockedBucket()

        mockMvc.perform(
            get(ApiRoutes.Product.BASE).with { it.remoteAddr = "10.0.0.50"; it }
        ).andExpect(status().isOk)

        mockMvc.perform(
            get(ApiRoutes.Product.BASE).with { it.remoteAddr = "10.0.0.99"; it }

        ).andExpect(status().isTooManyRequests)
    }

    @Test
    fun `rate limit resets after bucket is replenished`() {
        every { rateLimitService.resolveBucket(any()) } returns blockedBucket()
        mockMvc.perform(
            get(ApiRoutes.Product.BASE).with { it.remoteAddr = "10.0.0.5"; it }
        ).andExpect(status().isTooManyRequests)

        every { rateLimitService.resolveBucket(any()) } returns allowedBucket()
        mockMvc.perform(
            get(ApiRoutes.Product.BASE).with { it.remoteAddr = "10.0.0.5"; it }
        ).andExpect(status().isOk)
    }
}
