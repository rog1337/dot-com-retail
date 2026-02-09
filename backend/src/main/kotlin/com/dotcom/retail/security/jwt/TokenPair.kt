package com.dotcom.retail.security.jwt

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)