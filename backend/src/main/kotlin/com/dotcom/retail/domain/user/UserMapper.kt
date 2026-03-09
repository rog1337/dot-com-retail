package com.dotcom.retail.domain.user

import com.dotcom.retail.domain.user.dto.UserDetailsDto
import com.dotcom.retail.domain.user.dto.UserDto
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            displayName = user.displayName,
        )
    }

    fun toDetailsDto(user: User): UserDetailsDto {
        return UserDetailsDto(
            id = user.id,
            displayName = user.displayName,
            email = user.email,
        )
    }
}