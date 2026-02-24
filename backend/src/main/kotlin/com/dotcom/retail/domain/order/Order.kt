package com.dotcom.retail.domain.order

import com.dotcom.retail.common.BaseEntity
import com.dotcom.retail.domain.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "orders")
class Order(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false)
    var shippingName: String,

    @Column(nullable = false)
    var shippingAddress: String,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING_PAYMENT,

    @Column(nullable = false)
    var totalAmount: BigDecimal,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItem> = mutableListOf(),

) : BaseEntity() {

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