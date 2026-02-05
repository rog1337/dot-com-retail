package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus

abstract class AppException(
    override val message: String = "Error",
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
    cause: Throwable? = null
) : RuntimeException(message, cause)

open class NotFoundException(
    resourceName: String?,
    identifier: Any? = null
) : AppException(
    message = "$resourceName not found${identifier?.let { ": $it" } ?: ""}",
    status = HttpStatus.NOT_FOUND
)

open class AlreadyExistsException(
    resourceName: String?,
    identifier: Any? = null
) : AppException(
    message = "$resourceName already exists${identifier?.let { ": $it" } ?: ""}",
    status = HttpStatus.CONFLICT
)

open class BadRequestException(message: String = "Bad request") : AppException(message, HttpStatus.BAD_REQUEST)