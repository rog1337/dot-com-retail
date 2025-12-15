package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.common.BaseEntity
import com.dotcom.retail.domain.catalogue.image.Image
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class Brand(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    var image: Image? = null,

) : BaseEntity() {

    override fun toString(): String {
        return "Brand(id=$id, name='$name', image=$image, ${super.toString()})"
    }
}