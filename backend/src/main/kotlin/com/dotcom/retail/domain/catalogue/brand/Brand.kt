package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.common.model.AuditingEntity
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.domain.catalogue.product.Product
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class Brand(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    var image: Image? = null,

    @OneToMany(mappedBy = "brand", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonBackReference
    var products: MutableList<Product> = mutableListOf(),

    var isActive: Boolean = false

) : AuditingEntity() {

    override fun toString(): String {
        return "Brand(id=$id, name='$name', image=$image, ${super.toString()})"
    }
}