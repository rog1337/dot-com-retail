package com.dotcom.retail.common.exception

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

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
        return ProblemDetail.forStatusAndDetail(e.status, e.message)
    }

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidException(e: MethodArgumentNotValidException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(e.statusCode, e.message)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(e.statusCode, e.message)
    }

}