package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.brand.BrandMapper
import com.dotcom.retail.domain.catalogue.category.CategoryMapper
import com.dotcom.retail.domain.catalogue.image.ImageMapper
import org.springframework.stereotype.Component

@Component
class ProductMapper(
    private val imageMapper: ImageMapper,
    private val brandMapper: BrandMapper,
    private val categoryMapper: CategoryMapper
) {
    fun toDto(product: Product): ProductDto = ProductDto(
        id = product.id,
        name = product.name,
        sku = product.sku,
        slug = product.slug,
        description = product.description,
        price = product.price,
        salePrice = product.salePrice,
        stock = product.stock,
        brand = product.brand?.let { brandMapper.toDto(it) },
        category = product.category?.let { categoryMapper.toDto(it) },
        attributes = product.attributes,
        images = product.images.map { image -> imageMapper.toProductImageDto(image, product.id) },
        isActive = product.isActive,
    )
}