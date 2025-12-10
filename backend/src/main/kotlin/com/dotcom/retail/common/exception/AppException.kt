package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

abstract class AppException(
    override val message: String = "Error",
    val status: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)

open class ResourceNotFoundException(
    resourceName: String,
    identifier: Any? = null
) : AppException(
    if (identifier != null) "$resourceName not found: $identifier" else "$resourceName not found",
    status = HttpStatus.NOT_FOUND
)

open class AlreadyExistsException(
    resourceName: String,
    identifier: Any? = null
) : AppException(
    if (identifier != null) "$resourceName already exists: $identifier" else "$resourceName already exists",
    status = HttpStatus.CONFLICT
)