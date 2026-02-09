package com.dotcom.retail.domain.auth.dto

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)
