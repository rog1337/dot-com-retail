package com.dotcom.retail.common.exception

import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.stripe.exception.SignatureVerificationException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.ServletException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.resource.NoResourceFoundException

@Order(Ordered.HIGHEST_PRECEDENCE)
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

    @ExceptionHandler(AppException::class)
    fun handleAppException(e: AppException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(e.status, e.message)
        problemDetail.setProperty("code", e.code)
        return problemDetail
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(e: MethodArgumentNotValidException): ProblemDetail {
        val error = e.bindingResult.fieldErrors.firstOrNull() ?:
            return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request body")

        val message =
            if (error.isBindingFailure) "Field is missing or has incorrect type: ${error.field}"
            else "${error.field}: ${error.defaultMessage}"
        return ProblemDetail.forStatusAndDetail(e.statusCode, message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonError(e: HttpMessageNotReadableException): ProblemDetail {
        val cause = e.cause

        val message = if (cause is MismatchedInputException && !cause.path.isNullOrEmpty()) {
            val fieldName = cause.path?.first()?.fieldName

            when (cause) {
                is InvalidNullException -> "Field '$fieldName' is required"
                else -> "Invalid value for field '$fieldName'"
            }
        } else "Invalid request body"

        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(e.statusCode, e.message)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(e: NoResourceFoundException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupported(e: HttpMediaTypeNotSupportedException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.message)
    }

    @ExceptionHandler(MultipartException::class)
    fun handleMultipartException(e: MultipartException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid multipart request")
    }

    @ExceptionHandler(MissingServletRequestPartException::class)
    fun handleMissingServletRequestPart(e: MissingServletRequestPartException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
    }

    @ExceptionHandler(ServletException::class)
    fun handleServletException(e: ServletException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
    }

    @ExceptionHandler(SignatureVerificationException::class)
    fun handleSignatureVerificationException(e: SignatureVerificationException): ProblemDetail {
        val errorDetail = TransactionError.STRIPE_SIGNATURE_VERIFICATION_FAILED
        val problemDetail = ProblemDetail.forStatusAndDetail(errorDetail.status, errorDetail.message)
        problemDetail.setProperty("code", errorDetail.code)
        return problemDetail
    }
}