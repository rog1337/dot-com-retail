package com.dotcom.retail.common.constants

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

object SecurityConstants {
    var ACCESS_TOKEN_EXPIRATION_MS: Long = 15 * 60 * 1000 // 15 minutes, fallback value
    var REFRESH_TOKEN_EXPIRATION_MS: Long = 4 * 24 * 60 * 60 * 1000 // 4 days, fallback value
    const val BEARER_PREFIX = "Bearer "
    const val BEARER_TOKEN_START_INDEX = 7
    const val TOKEN_TYPE_CLAIM = "type"
    const val ACCESS_TOKEN_TYPE = "access_token"
    const val REFRESH_TOKEN_TYPE = "refresh_token"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val COOKIE_SAME_SITE_STRICT = "Strict"
    const val COOKIE_SAME_SITE_LAX = "Lax"
    const val COOKIE_PATH = "/"
    const val COOKIE_HEADER_NAME = "Set-Cookie"
}