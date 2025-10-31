package com.dotcom.retail.domain.user

fun User.toDto() = UserDto(
    id = id.toString(),
    email = email,
    displayName = displayName,
)