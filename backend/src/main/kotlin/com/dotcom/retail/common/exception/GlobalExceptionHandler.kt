package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler(
    private val exceptionService: ExceptionService
) {

//    @ExceptionHandler(Exception::class)
//    fun handleGlobalException(e: Exception, req: WebRequest): ResponseEntity<Any> {
//        return exceptionService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error")
//    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<String> {
        return ResponseEntity(e.message, e.statusCode)
    }

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    fun handleEmailAlreadyRegisteredException(e: EmailAlreadyRegisteredException): ResponseEntity<Any> {

        return exceptionService.createResponse(HttpStatus.CONFLICT, e.message, null)
    }

    @ExceptionHandler(EmailNotFoundException::class)
    fun handleEmailNotFoundException(e: EmailNotFoundException): ResponseEntity<Any> {
        return exceptionService.createResponse(HttpStatus.NOT_FOUND, e.message, null)
    }

}