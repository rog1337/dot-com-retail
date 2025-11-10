package com.dotcom.retail.common.exception

import com.dotcom.retail.common.exception.user.EmailAlreadyRegisteredException
import com.dotcom.retail.common.exception.user.EmailNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

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

}