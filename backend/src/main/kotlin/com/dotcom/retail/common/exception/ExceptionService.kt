package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.context.request.WebRequest
import java.time.Instant

@Service
class ExceptionService {

    fun createErrorResponse(status: HttpStatus, msg: String? = null, req: WebRequest? = null): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = status.value(),
            message = msg,
            error = status,
            timestamp = Instant.now(),
            path = getPath(req)
        )
        return ResponseEntity(response, status)
    }

    private fun getPath(req: WebRequest?): String? {
        if (req == null) return null
        return req.getDescription(false).removePrefix("uri=")
    }
}