package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.domain.catalogue.product.Product
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class ProductImage(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    var url: String?,

    @ManyToOne
    @JoinColumn(name = "product_id")
    var product: Product? = null,
)