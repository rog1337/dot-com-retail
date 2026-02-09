package com.dotcom.retail.domain.auth.dto

data class RegisterOAuthUser(
    val email: String,
    val displayName: String,
    val pictureUrl: String?,
)