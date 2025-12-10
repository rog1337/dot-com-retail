package com.dotcom.retail.common.exception

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(e: Exception): ProblemDetail {
        logger.error("Exception: ${e.printStackTrace()}")
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.")
    }

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid JWT")
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Expired JWT")
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(e: ResourceNotFoundException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(e.status, e.message)
    }

    @ExceptionHandler(AppException::class)
    fun handleAppException(e: AppException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)
    }

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)
    }

//    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
//    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<String> {
//        return ResponseEntity(e.message, e.statusCode)
//    }

}