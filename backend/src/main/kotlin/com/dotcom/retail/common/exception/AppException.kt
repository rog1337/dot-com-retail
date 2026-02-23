package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus

open class AppException(
    error: AppError,
    val code: String = error.code,
    val status: HttpStatus = error.status,
    val identifier: Any? = error.identifier,
    override val message: String = formatMessage(error.message, identifier)
) : RuntimeException(error.message)

fun formatMessage(message: String, identifier: Any?): String {
    return identifier?.let { return "$message: $it" } ?: message
}