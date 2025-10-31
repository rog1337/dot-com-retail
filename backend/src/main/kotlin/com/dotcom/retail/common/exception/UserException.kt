package com.dotcom.retail.common.exception

class EmailAlreadyRegisteredException(
    val email: String?,
    message: String = "Email already registered: $email"
) : RuntimeException(message)