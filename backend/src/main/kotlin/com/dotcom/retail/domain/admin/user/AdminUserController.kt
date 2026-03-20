package com.dotcom.retail.domain.admin.user

import com.dotcom.retail.common.constants.ApiRoutes.Admin
import com.dotcom.retail.domain.admin.user.dto.AdminUserDto
import com.dotcom.retail.domain.admin.user.dto.AdminUserUpdateRequest
import com.dotcom.retail.domain.user.UserMapper
import com.dotcom.retail.domain.user.UserService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(Admin.User.BASE)
class AdminUserController(
    private val userService: UserService,
    private val userMapper: UserMapper,
    private val adminUserService: AdminUserService
) {

    @GetMapping("{userId}")
    fun getUserById(@PathVariable userId: UUID): ResponseEntity<AdminUserDto> {
        val user = userService.getById(userId)
        return ok(userMapper.toAdminDto(user))
    }

    @PatchMapping("{userId}")
    fun updateUser(
        @PathVariable userId: UUID,
        @RequestBody request: AdminUserUpdateRequest,
    ): ResponseEntity<AdminUserDto> {
        val user = adminUserService.updateUser(userId, request)
        return ok(userMapper.toAdminDto(user))
    }
}