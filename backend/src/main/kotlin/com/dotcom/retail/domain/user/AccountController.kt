package com.dotcom.retail.domain.user

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.common.util.pagination.PageConstants
import com.dotcom.retail.common.util.pagination.PageMapper
import com.dotcom.retail.common.util.pagination.PagedResponse
import com.dotcom.retail.domain.auth.AuthService
import com.dotcom.retail.domain.auth.PasswordResetService
import com.dotcom.retail.domain.order.Order
import com.dotcom.retail.domain.order.OrderMapper
import com.dotcom.retail.domain.order.dto.OrderDto
import com.dotcom.retail.domain.user.dto.UserDetailsDto
import com.dotcom.retail.domain.user.dto.UserDto
import com.dotcom.retail.domain.user.dto.UserUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiRoutes.Account.BASE)
class AccountController(
    private val userService: UserService,
    private val userMapper: UserMapper,
    private val orderMapper: OrderMapper,
    private val authService: AuthService,
    private val passwordResetService: PasswordResetService
) {

    @GetMapping
    fun getAccount(
        @AuthenticationPrincipal userId: UUID,
    ): ResponseEntity<UserDto> {
        val user = userService.getById(userId)
        return ResponseEntity.ok().body(userMapper.toDto(user))
    }

    @GetMapping(ApiRoutes.Account.DETAILS)
    fun getAccountDetails(
        @AuthenticationPrincipal userId: UUID,
    ): ResponseEntity<UserDetailsDto> {
        val user = userService.getById(userId)
        return ResponseEntity.ok().body(userMapper.toDetailsDto(user))
    }

    @GetMapping(ApiRoutes.Account.ORDERS)
    fun getAccountOrders(
        @AuthenticationPrincipal userId: UUID,
        @RequestParam(required = false) page: Int = PageConstants.DEFAULT_PAGE,
        @RequestParam(required = false) pageSize: Int = PageConstants.DEFAULT_PAGE_SIZE,
    ): ResponseEntity<PagedResponse<OrderDto>> {
        val orders = userService.getOrders(userId, page, pageSize)
        val dtos = orders.map { orderMapper.toDto(it) }
        return ResponseEntity.ok().body(PageMapper.toPagedResponse(dtos))
    }

    @PatchMapping
    fun updateAccount(
        @AuthenticationPrincipal userId: UUID,
        @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<UserDto> {
        val user = userService.updateUser(userId, request)
        return ResponseEntity.ok().body(userMapper.toDto(user))
    }

    @PostMapping(ApiRoutes.Auth.RESET_PASSWORD)
    fun resetPassword(
        @AuthenticationPrincipal userId: UUID,
    ): ResponseEntity<Void> {
        passwordResetService.initiatePasswordResetByUserId(userId)
        return ResponseEntity.ok().build<Void>()
    }
}