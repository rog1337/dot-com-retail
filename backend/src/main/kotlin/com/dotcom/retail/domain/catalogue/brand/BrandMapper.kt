package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.domain.admin.brand.dto.AdminBrandDto
import com.dotcom.retail.domain.catalogue.image.ImageMapper
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class BrandMapper(
    private val imageMapper: ImageMapper,
) {
    fun toDto(brand: Brand): BrandDto = BrandDto(
        id = brand.id,
        name = brand.name,
        image = brand.image?.let { imageMapper.toBrandImageDto(it) },
        isActive = brand.isActive,
    )

    fun toAdminDto(brand: Brand): AdminBrandDto = AdminBrandDto(
        id = brand.id,
        name = brand.name,
        image = brand.image?.let { imageMapper.toBrandImageDto(it) },
        isActive = brand.isActive,
    )

    fun toPagedAdminDto(brands: Page<Brand>): Page<AdminBrandDto> =
        brands.map { toAdminDto(it) }
}
