package com.dotcom.retail.domain.user

import com.dotcom.retail.common.ContactMapper
import com.dotcom.retail.domain.admin.user.dto.AdminUserDto
import com.dotcom.retail.domain.user.dto.UserDetailsDto
import com.dotcom.retail.domain.user.dto.UserDto
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val contactMapper: ContactMapper
) {

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
            contact = user.contact?.let { contactMapper.decryptContact(it.contact) }
        )
    }

    fun toAdminDto(u: User): AdminUserDto = AdminUserDto(
        id = u.id,
        displayName = u.displayName,
        email = u.email,
        contact = u.contact?.let { contactMapper.decryptContact(it.contact) },
        twoFactorEnabled = u.twoFactorEnabled,
        role = u.role,
    )
}