package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

//    @ExceptionHandler(Exception::class)
//    fun handleGlobalException(e: Exception, req: WebRequest): ResponseEntity<String> {
//        return ResponseEntity("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR)
//    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<String> {
        return ResponseEntity(e.message, e.statusCode)
    }

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    fun handleEmailAlreadyRegisteredException(e: EmailAlreadyRegisteredException): ResponseEntity<Any> {

        return createResponse(HttpStatus.CONFLICT, e.message, null)
    }

    fun createResponse(status: HttpStatus, msg: String?, err: String?): ResponseEntity<Any> {
        val response = mapOf(
            "message" to msg,
            "error" to err,
            "status" to status.toString(),
            "timestamp" to Instant.now()
        )
        return ResponseEntity(response, status)
    }

}