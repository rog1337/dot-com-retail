package com.dotcom.retail.domain.user.dto

import java.util.UUID

data class CreateUserParams(
    val email: String,
    val displayName: String? = null,
    val password: String? = null,
    val pictureUrl: String? = null,
)

data class UserDto(
    val id: UUID,
    val displayName: String
)

data class UserUpdateRequest(
    val displayName: String,
)

data class UserDetailsDto(
    val id: UUID,
    val displayName: String,
    val email: String,
)