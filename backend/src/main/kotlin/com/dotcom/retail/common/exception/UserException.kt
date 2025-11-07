package com.dotcom.retail.common.exception

class EmailAlreadyRegisteredException(
    val email: String?,
    message: String = "Email already registered: $email"
) : RuntimeException(message)

class EmailNotFoundException(
    val email: String?,
    message: String = "Email not found: $email"
) : RuntimeException(message)