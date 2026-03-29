package com.dotcom.retail.domain.admin.order

import com.dotcom.retail.common.constants.ApiRoutes.Admin.Order
import com.dotcom.retail.domain.admin.order.dto.AdminOrderDto
import com.dotcom.retail.domain.admin.order.dto.OrderCancelRequest
import com.dotcom.retail.domain.admin.order.dto.OrderUpdateRequest
import com.dotcom.retail.domain.order.OrderMapper
import com.dotcom.retail.domain.order.OrderService
import com.dotcom.retail.domain.payment.PaymentService
import com.dotcom.retail.domain.payment.dto.RefundRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(Order.BASE)
class AdminOrderController(
    private val orderService: OrderService,
    private val orderMapper: OrderMapper,
    private val adminOrderService: AdminOrderService,
    private val paymentService: PaymentService
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

    @PostMapping("{orderId}" + Order.CANCEL)
    fun cancelOrder(
        @PathVariable orderId: UUID,
        @RequestBody request: OrderCancelRequest,
    ): ResponseEntity<Void> {
        adminOrderService.cancelOrder(orderId, request)
        return ok().build()
    }

    @PostMapping("{orderId}" + Order.REFUND)
    fun refund(
        @PathVariable orderId: UUID,
        @Valid @RequestBody request: RefundRequest?,
    ): ResponseEntity<Void> {
        paymentService.initiateRefund(orderId, request)
        return ResponseEntity.accepted().build()
    }

}