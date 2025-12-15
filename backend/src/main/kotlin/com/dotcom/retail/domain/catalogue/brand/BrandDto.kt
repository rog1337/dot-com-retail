package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.domain.catalogue.image.ImageDto

data class BrandDto(
    val id: Long,
    val name: String,
    val image: ImageDto?,
)