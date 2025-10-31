package com.dotcom.retail.domain.user

data class UserDto(
    val id: String,
    val email: String,
    val displayName: String
)

data class CreateUserParams(
    val email: String,
    val displayName: String? = null,
    val password: String? = null,
    val pictureUrl: String? = null,
)