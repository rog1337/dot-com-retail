package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.domain.catalogue.image.toDto

fun Brand.toDto(): BrandDto = BrandDto(
    id = id,
    name = name,
    image = image?.toDto()
)