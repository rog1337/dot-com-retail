package com.dotcom.retail.domain.order

import com.dotcom.retail.common.model.AuditingEntity
import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.domain.user.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "orders")
class Order(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    var sessionId: String? = null,
    var intentId: String,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING_PAYMENT,

    @Embedded
    var contact: Contact? = null,

    var notes: String? = null,

    @Enumerated(EnumType.STRING)
    var shippingType: ShippingType? = ShippingType.STANDARD,

    var shippingCost: BigDecimal? = null,

    @Column(nullable = false)
    var totalAmount: BigDecimal,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItem> = mutableListOf(),

    ) : AuditingEntity() {

    fun addItem(item: OrderItem) {
        items.add(item)
        item.order = this
    }

}

enum class OrderStatus {
    PENDING_PAYMENT,
    PAID,
    FAILED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

enum class ShippingType {
    STANDARD,
    EXPRESS
}