package com.dotcom.retail.domain.cart

import com.dotcom.retail.domain.catalogue.product.Product
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "cart_items")
class CartItem(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    var priceSnapshot: BigDecimal,

    @Column(nullable = false)
    var quantity: Int = 1,
) {
    fun getTotalPrice(): BigDecimal {
        return priceSnapshot.multiply(BigDecimal(quantity))
    }

    override fun toString(): String {
        return "CartItem(id=$id, product=$product, quantity=$quantity)"
    }
}