package com.dotcom.retail.common.exception.jwt

open class JwtException : RuntimeException {
    companion object {
        private const val DEFAULT_MSG = "Jwt error"
    }
    constructor() : super(DEFAULT_MSG)
    constructor(message: String) : super(message)
}

class InvalidRefreshTokenException : JwtException {
    companion object {
        private const val DEFAULT_MSG = "Invalid refresh token"
    }
    constructor() : super(DEFAULT_MSG)
}
