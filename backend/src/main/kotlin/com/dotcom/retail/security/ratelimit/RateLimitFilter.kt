package com.dotcom.retail.security.ratelimit

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

@Component
class RateLimitFilter(private val rateLimitService: RateLimitService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val key = request.remoteAddr
        val bucket = rateLimitService.resolveBucket(key)
        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            filterChain.doFilter(request, response)
        } else {
            val retryAfterSeconds = Duration.ofNanos(probe.nanosToWaitForRefill).toSeconds()
            val problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "You have exceeded the request limit. Please try again later."
            )
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", retryAfterSeconds.toString())
            response.contentType = "application/json"
            response.writer.write(ObjectMapper().writeValueAsString(problem))
            return
        }
    }
}