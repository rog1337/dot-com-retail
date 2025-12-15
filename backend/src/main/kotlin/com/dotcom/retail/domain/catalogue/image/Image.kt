package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Image(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    var url: String,

) : BaseEntity() {

    override fun toString(): String {
        return "Image(id=$id, url='$url')"
    }
}