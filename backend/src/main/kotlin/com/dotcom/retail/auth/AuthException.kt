package com.dotcom.retail.auth

class OAuth2EmailNotVerifiedException : RuntimeException {
    companion object {
        private const val DEFAULT_MSG = "Email not verified"
    }
    constructor() : super("${AuthConstants.OAUTH2_NAME} $DEFAULT_MSG")
    constructor(provider: String) : super("$provider $DEFAULT_MSG")
    constructor(provider: String?, message: Any) : super("$provider $DEFAULT_MSG: $message")
}

class OAuth2Exception : RuntimeException {
    companion object {
        private const val DEFAULT_MSG = "Authentication failed"
    }
    constructor() : super("${AuthConstants.OAUTH2_NAME} $DEFAULT_MSG")
    constructor(message: Any) : super("${AuthConstants.OAUTH2_NAME} $DEFAULT_MSG: $message")
}