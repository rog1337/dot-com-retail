package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.admin.product.dto.AdminProductCategoryDto
import com.dotcom.retail.domain.admin.product.dto.AdminProductDto
import com.dotcom.retail.domain.catalogue.brand.BrandMapper
import com.dotcom.retail.domain.catalogue.image.ImageMapper
import org.springframework.data.domain.Page
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
        category = p.category?.let { ProductCategoryDto(it.id, it.name) },
        attributes = p.attributes?.map { ProductAttributeDto(name = it.key, values = it.value)},
        images = p.images.map { image -> imageMapper.toProductImageDto(image) },
        reviewCount = p.reviewCount,
        averageRating = p.averageRating,
        isActive = p.isActive,
    )

    fun queryParamsToQuery(params: ProductQueryParams, attributes: List<ProductAttributeDto>): ProductQuery {
        return ProductQuery(
            categoryId = params.categoryId,
            brands = params.brands,
            attributes = attributes,
            page = params.page,
            size = params.size,
            sort = params.sort,
            price = params.price
        )
    }

    fun toAdminDto(p: Product): AdminProductDto = AdminProductDto(
        id = p.id,
        name = p.name,
        sku = p.sku,
        description = p.description,
        price = p.price,
        salePrice = p.salePrice,
        stock = p.stock,
        brand = p.brand?.let { brandMapper.toDto(it) },
        category = p.category?.let { AdminProductCategoryDto(id = it.id, name = it.name) },
        attributes = p.attributes?.map { ProductAttributeDto(name = it.key, values = it.value)},
        images = p.images.map { image -> imageMapper.toProductImageDto(image) },
        reviewCount = p.reviewCount,
        averageRating = p.averageRating,
        isActive = p.isActive,
    )

    fun toPagedAdminDto(page: Page<Product>): Page<AdminProductDto> {
        return page.map { toAdminDto(it) }
    }
}