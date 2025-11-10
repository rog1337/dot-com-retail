package com.dotcom.retail.common.exception.user

open class UserException : RuntimeException {
    companion object {
        const val DEFAULT_MSG = "User error"
    }
    constructor() : super(DEFAULT_MSG)
    constructor(message: String) : super(message)
}

class UserNotFoundException : UserException {
    companion object {
        const val DEFAULT_MSG = "User not found"
    }
    constructor() : super(DEFAULT_MSG)
    constructor(message: String) : super("$DEFAULT_MSG: $message")
}

class EmailAlreadyRegisteredException(
    val email: String?,
    message: String = "Email already registered: $email"
) : UserException(message)

class EmailNotFoundException(
    val email: String?,
    message: String = "Email not found: $email"
) : UserException(message)