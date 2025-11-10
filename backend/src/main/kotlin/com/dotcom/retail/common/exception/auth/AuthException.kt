package com.dotcom.retail.common.exception.auth

import com.dotcom.retail.common.constants.AuthConstants

open class AuthException : RuntimeException {
    companion object {
        private const val DEFAULT_MSG = "Authentication error"
    }
    constructor(message: String?) : super(message ?: DEFAULT_MSG)
}

class OAuth2EmailNotVerifiedException : AuthException {
    companion object {
        private const val DEFAULT_MSG = "Email not verified"
    }
    constructor() : super("${AuthConstants.OAUTH2_NAME} $DEFAULT_MSG")
    constructor(provider: String) : super("$provider $DEFAULT_MSG")
    constructor(provider: String?, message: Any) : super("$provider $DEFAULT_MSG: $message")
}

class OAuth2Exception : AuthException {
    companion object {
        private const val DEFAULT_MSG = "Authentication failed"
    }
    constructor() : super("${AuthConstants.OAUTH2_NAME} $DEFAULT_MSG")
    constructor(message: Any) : super("${AuthConstants.OAUTH2_NAME} $DEFAULT_MSG: $message")
}

class InvalidLoginException : AuthException {
    companion object {
        private const val DEFAULT_MSG = "Invalid email or password"
    }
    constructor() : super(DEFAULT_MSG)
    constructor(message: String) : super(message)
}