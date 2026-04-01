package com.dotcom.retail.domain.order

import com.dotcom.retail.common.ContactMapper
import com.dotcom.retail.domain.admin.order.dto.AdminOrderDto
import com.dotcom.retail.domain.catalogue.image.ImageMapper
import com.dotcom.retail.domain.order.dto.OrderDto
import com.dotcom.retail.domain.order.dto.OrderItemDto
import org.springframework.stereotype.Component

@Component
class OrderMapper(
    private val imageMapper: ImageMapper,
    private val contactMapper: ContactMapper
) {
    fun toDto(o: Order): OrderDto = OrderDto(
        id = o.id,
        date = o.createdAt.toEpochMilli(),
        status = o.status,
        totalAmount = o.totalAmount,
        items = o.items.map { toOrderItemDto(it) },
        paymentId = o.intentId,
        sessionId = o.sessionId,
        shippingType = o.shippingType,
        shippingCost = o.shippingCost,
        contact = o.contact?.let { contactMapper.decryptContact(it) },
        notes = o.notes
    )

    fun toOrderItemDto(o: OrderItem): OrderItemDto {
        val image = o.product.images.firstOrNull()
        return OrderItemDto(
            productId = o.product.id,
            productName = o.productName,
            image = image?.let { imageMapper.toProductImageDto(image) },
            quantity = o.quantity,
            price = o.price,
            totalAmount = o.totalAmount(),
        )
    }

    fun toAdminDto(o: Order): AdminOrderDto = AdminOrderDto(
        id = o.id,
        date = o.createdAt.toEpochMilli(),
        status = o.status,
        totalAmount = o.totalAmount,
        items = o.items.map { toOrderItemDto(it) },
        paymentId = o.intentId,
        sessionId = o.sessionId,
        shippingType = o.shippingType,
        shippingCost = o.shippingCost,
        contact = o.contact?.let { contactMapper.decryptContact(it) },
        notes = o.notes
    )

}