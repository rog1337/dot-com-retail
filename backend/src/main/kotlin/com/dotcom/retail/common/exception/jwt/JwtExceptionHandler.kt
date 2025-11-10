package com.dotcom.retail.common.exception.jwt

import com.dotcom.retail.common.exception.ErrorResponse
import com.dotcom.retail.common.exception.ExceptionService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class JwtExceptionHandler(private val exceptionService: ExceptionService) {

    companion object {
        const val DEFAULT_ERROR_MSG = "Jwt error"
        const val TOKEN_EXPIRED_MSG = "Token expired"
        const val INVALID_TOKEN_MSG = "Invalid token"
    }

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(req: WebRequest): ResponseEntity<ErrorResponse> {
        return exceptionService.createErrorResponse(HttpStatus.UNAUTHORIZED, DEFAULT_ERROR_MSG, req)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(req: WebRequest): ResponseEntity<ErrorResponse> {
        return exceptionService.createErrorResponse(HttpStatus.UNAUTHORIZED, TOKEN_EXPIRED_MSG, req)
    }

    @ExceptionHandler(MalformedJwtException::class)
    fun handleMalformedJwtException(req: WebRequest): ResponseEntity<ErrorResponse> {
        return exceptionService.createErrorResponse(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG, req)
    }

    @ExceptionHandler(SignatureException::class)
    fun handleSignatureException(req: WebRequest): ResponseEntity<ErrorResponse> {
        return exceptionService.createErrorResponse(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG, req)
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(req: WebRequest): ResponseEntity<ErrorResponse> {
        return exceptionService.createErrorResponse(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG, req)
    }
}