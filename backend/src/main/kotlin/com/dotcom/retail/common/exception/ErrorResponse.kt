package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus
import java.time.Instant

data class ErrorResponse(
    val status: Int,
    val message: String? = null,
    val error: HttpStatus,
    val timestamp: Instant,
    val path: String? = null,
)