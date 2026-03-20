package com.dotcom.retail.domain.admin.brand.dto

import com.dotcom.retail.domain.catalogue.image.ImageDto

data class AdminBrandDto(
    val id: Long,
    val name: String,
    val image: ImageDto?,
    val isActive: Boolean
)

data class CreateBrand(
    val name: String,
    val image: Long? = null,
    val isActive: Boolean
)

data class EditBrand(
    val id: Long,
    val name: String,
    val image: Long? = null,
    val isActive: Boolean,
)