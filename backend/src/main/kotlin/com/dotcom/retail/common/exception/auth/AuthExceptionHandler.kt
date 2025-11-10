package com.dotcom.retail.common.exception.auth

import com.dotcom.retail.common.exception.ErrorResponse
import com.dotcom.retail.common.exception.ExceptionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class AuthExceptionHandler(private val exceptionService: ExceptionService) {

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): ResponseEntity<ErrorResponse> {
        return exceptionService.createErrorResponse(HttpStatus.UNAUTHORIZED, e.message)
    }
}