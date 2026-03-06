package com.dotcom.retail.domain.cart

import com.dotcom.retail.common.model.AuditingEntity
import com.dotcom.retail.domain.order.ShippingType
import com.dotcom.retail.domain.user.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "carts")
class Cart(
    @Id
    val id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    var sessionId: String? = null,
    var intentId: String? = null,

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableSet<CartItem> = mutableSetOf<CartItem>(),

    @Enumerated(EnumType.STRING)
    var shippingType: ShippingType? = null,

    var shippingCost: BigDecimal? = null,

) : AuditingEntity() {
    fun addItem(item: CartItem) {
        items.add(item)
        item.cart = this
    }

    fun removeItem(item: CartItem) {
        items.remove(item)
        item.cart = null
    }

    fun getSubTotalPrice(): BigDecimal {
        return items.sumOf { it.getTotalPrice() }
    }

    fun getTotalPrice(): BigDecimal {
        return items.sumOf { it.getTotalPrice() }.add(shippingCost ?: BigDecimal.ZERO)
    }

    fun getTotalQuantity(): Int {
        return items.sumOf { it.quantity }
    }

    override fun toString(): String {
        return "Cart(id=$id, user=$user, sessionId=$sessionId) ${super.toString()}"
    }
}

