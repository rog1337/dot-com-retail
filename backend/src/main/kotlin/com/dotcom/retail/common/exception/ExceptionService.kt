package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ExceptionService {

    fun createResponse(status: HttpStatus, msg: String? = null, err: String? = null): ResponseEntity<Any> {
        val response = mapOf(
            "message" to msg,
            "error" to err,
            "status" to status.value(),
            "timestamp" to Instant.now()
        )
        return ResponseEntity(response, status)
    }
}