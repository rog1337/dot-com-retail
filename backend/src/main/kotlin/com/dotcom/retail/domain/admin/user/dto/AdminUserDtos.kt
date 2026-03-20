package com.dotcom.retail.domain.admin.user.dto

import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.domain.user.Role
import java.util.*

data class AdminUserDto(
    val id: UUID,
    val email: String,
    val displayName: String,
    val contact: Contact?,
    val twoFactorEnabled: Boolean,
    val role: Role,
)

data class AdminUserUpdateRequest(
    val role: Role,
)