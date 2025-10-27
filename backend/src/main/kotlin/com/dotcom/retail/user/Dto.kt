package com.dotcom.retail.user

import java.util.UUID

data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String
)