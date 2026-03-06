package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.brand.BrandMapper
import com.dotcom.retail.domain.catalogue.image.ImageMapper
import org.springframework.stereotype.Component

@Component
class ProductMapper(
    private val imageMapper: ImageMapper,
    private val brandMapper: BrandMapper,
) {
    fun toDto(p: Product): ProductDto = ProductDto(
        id = p.id,
        name = p.name,
        sku = p.sku,
        description = p.description,
        price = p.price,
        salePrice = p.salePrice,
        stock = p.stock,
        brand = p.brand?.let { brandMapper.toDto(it) },
        category = p.category?.let { ProductCategoryDto(it.id) },
        attributes = p.attributes?.map { ProductAttributeDto(name = it.key, values = it.value)},
        images = p.images.map { image -> imageMapper.toProductImageDto(image) },
        isActive = p.isActive,
    )

    fun queryParamsToQuery(params: ProductQueryParams, attributes: List<ProductAttributeDto>): ProductQuery {
        return ProductQuery(
            categoryId = params.categoryId,
            brands = params.brands,
            attributes = attributes,
            page = params.page,
            pageSize = params.pageSize,
            sort = params.sort,
            price = params.price
        )
    }
}