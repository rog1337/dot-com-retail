package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.domain.catalogue.image.ImageMapper
import org.springframework.stereotype.Component

@Component
class BrandMapper(
    private val imageMapper: ImageMapper,
) {
    fun toDto(brand: Brand): BrandDto = BrandDto(
        id = brand.id,
        name = brand.name,
        image = brand.image?.let { imageMapper.toBrandImageDto(it, brand.id) },
        isActive = brand.isActive,
    )
}
