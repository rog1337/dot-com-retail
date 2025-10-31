package com.dotcom.retail.domain.auth.dto

import com.dotcom.retail.domain.user.UserDto

data class AuthResponse(
    val accessToken: String,
    val user: UserDto,
)