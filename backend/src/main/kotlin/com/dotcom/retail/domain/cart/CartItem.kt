package com.dotcom.retail.domain.cart

import com.dotcom.retail.domain.catalogue.product.Product
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "cart_items")
class CartItem(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    var quantity: Int = 1,
) {
    fun getTotalPrice(): BigDecimal {
        return product.price.multiply(BigDecimal(quantity))
    }

    override fun toString(): String {
        return "CartItem(id=$id, product=$product, quantity=$quantity)"
    }
}