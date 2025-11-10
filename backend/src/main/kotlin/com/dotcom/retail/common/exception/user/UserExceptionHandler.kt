package com.dotcom.retail.common.exception.user

import com.dotcom.retail.common.exception.ErrorResponse
import com.dotcom.retail.common.exception.ExceptionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class UserExceptionHandler(private val exceptionService: ExceptionService) {


    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException, req: WebRequest): ResponseEntity<ErrorResponse> {
        return exceptionService.createErrorResponse(HttpStatus.NOT_FOUND, ex.message, req)
    }

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    fun handleEmailAlreadyRegisteredException(e: EmailAlreadyRegisteredException, req: WebRequest): ResponseEntity<ErrorResponse> {

        return exceptionService.createErrorResponse(HttpStatus.CONFLICT, e.message, req)
    }

    @ExceptionHandler(EmailNotFoundException::class)
    fun handleEmailNotFoundException(e: EmailNotFoundException, req: WebRequest): ResponseEntity<ErrorResponse> {
        return exceptionService.createErrorResponse(HttpStatus.NOT_FOUND, e.message, req)
    }
}