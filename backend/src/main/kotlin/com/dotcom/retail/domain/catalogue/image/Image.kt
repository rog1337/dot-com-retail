package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.model.AuditingEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Image(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    var fileName: String,
    var contentType: String,
    var sortOrder: Int,
    var altText: String? = null,

    ) : AuditingEntity() {

    override fun toString(): String {
        return "Image(id=$id, fileName='$fileName', contentType='$contentType', sortOrder=$sortOrder) ${super.toString()}"
    }
}