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
    var filePath: String,
    var contentType: String,
    var sortOrder: Int,

    ) : BaseEntity() {

    override fun toString(): String {
        return "Image(id=$id, filePath='$filePath', contentType='$contentType', sortOrder=$sortOrder) ${super.toString()}"
    }
}