package com.dotcom.retail.domain.admin.order

import com.dotcom.retail.common.constants.ApiRoutes.Admin
import com.dotcom.retail.domain.admin.order.dto.AdminOrderDto
import com.dotcom.retail.domain.admin.order.dto.OrderCancelRequest
import com.dotcom.retail.domain.admin.order.dto.OrderUpdateRequest
import com.dotcom.retail.domain.order.OrderMapper
import com.dotcom.retail.domain.order.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(Admin.Order.BASE)
class AdminOrderController(
    private val orderService: OrderService,
    private val orderMapper: OrderMapper,
    private val adminOrderService: AdminOrderService
) {

    @GetMapping("{orderId}")
    fun getOrderById(@PathVariable orderId: UUID): ResponseEntity<AdminOrderDto> {
        val order = orderService.getById(orderId)
        return ok(orderMapper.toAdminDto(order))
    }

    @PatchMapping("{orderId}")
    fun updateOrder(
        @PathVariable orderId: UUID,
        @RequestBody request: OrderUpdateRequest
    ): ResponseEntity<AdminOrderDto> {
        val order = adminOrderService.updateOrder(orderId, request)
        return ok(orderMapper.toAdminDto(order))
    }

    @PostMapping("{orderId}")
    fun cancelOrder(
        @PathVariable orderId: UUID,
        @RequestBody request: OrderCancelRequest,
    ): ResponseEntity<Void> {
        val order = orderService.getById(orderId)
        orderService.handleCancel(order, request.reason)
        return ok().build()
    }
}