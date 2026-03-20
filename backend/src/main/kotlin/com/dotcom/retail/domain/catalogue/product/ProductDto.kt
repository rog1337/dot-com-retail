package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.util.pagination.PageConstants
import com.dotcom.retail.domain.catalogue.brand.BrandDto
import com.dotcom.retail.domain.catalogue.filter.RangeData
import com.dotcom.retail.domain.catalogue.image.ImageDto
import java.math.BigDecimal

data class ProductDto (
    val id: Long,
    val name: String,
    val description: String?,
    val sku: String,
    val price: BigDecimal,
    val salePrice: BigDecimal,
    val stock: Int,
    val brand: BrandDto?,
    val category: ProductCategoryDto?,
    val attributes: List<ProductAttributeDto>?,
    val images: List<ImageDto>?,
    val reviewCount: Int?,
    val averageRating: Double?,
    val isActive: Boolean,
)

data class ProductAttributeDto(
    val name: String,
    val values: List<Any>
)

data class ProductBrandCount(
    val id: Long,
    val name: String,
    val count: Long
)

data class ProductQueryParams(
    val categoryId: Long?,
    val brands: List<Long> = emptyList(),
    val page: Int = PageConstants.DEFAULT_PAGE,
    val pageSize: Int = PageConstants.DEFAULT_PAGE_SIZE,
    val sort: ProductSortOrder = ProductSortOrder.TOP,
    val price: RangeData? = null
)

data class ProductQuery(
    val categoryId: Long?,
    val brands: List<Long> = emptyList(),
    val attributes: List<ProductAttributeDto> = emptyList(),
    val page: Int = PageConstants.DEFAULT_PAGE,
    val pageSize: Int = PageConstants.DEFAULT_PAGE_SIZE,
    val sort: ProductSortOrder = ProductSortOrder.TOP,
    val price: RangeData? = null
)

data class ProductCategoryDto(
    val id: Long,
)