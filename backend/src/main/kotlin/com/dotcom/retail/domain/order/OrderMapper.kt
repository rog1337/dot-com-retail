package com.dotcom.retail.domain.order

import com.dotcom.retail.common.ContactMapper
import com.dotcom.retail.common.model.AddressFields
import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.common.service.EncryptionService
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
        items = o.items.map { toCartItemDto(it) },
        paymentId = o.intentId,
        sessionId = o.sessionId,
        shippingType = o.shippingType,
        shippingCost = o.shippingCost,
        contact = o.contact?.let { contactMapper.decryptContact(it) },
        notes = o.notes
    )

    fun toCartItemDto(o: OrderItem): OrderItemDto {
        val image = o.product.images.firstOrNull()
        return OrderItemDto(
            productId = o.product.id,
            productName = o.productName,
            imageUrl = image?.let { imageMapper.toCartImageDto(image) },
            quantity = o.quantity,
            price = o.price,
            totalAmount = o.totalAmount(),
        )
    }
}