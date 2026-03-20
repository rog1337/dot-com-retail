package com.dotcom.retail.domain.admin.user

import com.dotcom.retail.domain.admin.user.dto.AdminUserUpdateRequest
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserRepository
import com.dotcom.retail.domain.user.UserService
import com.dotcom.retail.security.jwt.JwtService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdminUserService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) {

    @Transactional
    fun updateUser(userId: UUID, request: AdminUserUpdateRequest): User {
        val user = userService.getById(userId)
        user.role = request.role
        userRepository.save(user)
        jwtService.revokeTokens(userId)
        return user
    }
}