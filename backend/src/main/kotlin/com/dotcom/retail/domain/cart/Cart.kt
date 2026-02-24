package com.dotcom.retail.domain.cart

import com.dotcom.retail.common.BaseEntity
import com.dotcom.retail.domain.user.User
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "carts")
class Cart(
    @Id
    val id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    var sessionId: String? = null,

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<CartItem> = mutableListOf<CartItem>(),

) : BaseEntity() {
    fun addItem(item: CartItem) {
        items.add(item)
        item.cart = this
    }

    fun removeItem(item: CartItem) {
        items.remove(item)
        item.cart = null
    }

    fun getTotalPrice(): BigDecimal {
        return items.sumOf { it.getTotalPrice() }
    }

    override fun toString(): String {
        return "Cart(id=$id, user=$user, sessionId=$sessionId) ${super.toString()}"
    }
}

