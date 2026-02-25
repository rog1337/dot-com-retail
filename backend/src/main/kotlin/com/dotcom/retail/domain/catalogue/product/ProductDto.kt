package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.model.SortOrder
import com.dotcom.retail.common.util.pagination.PageConstants
import com.dotcom.retail.domain.catalogue.brand.BrandDto
import com.dotcom.retail.domain.catalogue.filter.RangeData
import com.dotcom.retail.domain.catalogue.image.EditImage
import com.dotcom.retail.domain.catalogue.image.ImageDto
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import jakarta.validation.constraints.NotBlank
import org.openapitools.jackson.nullable.JsonNullable
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
    val isActive: Boolean,
)

data class ProductAttributeDto(
    val name: String,
    val values: List<Any>
)

data class CreateProduct (
    @field:NotBlank(message = "Product name cannot be blank")
    val name: String,
    @field:NotBlank(message = "Product sku cannot be blank")
    val sku: String,
    val description: String?,
    val price: BigDecimal = BigDecimal.ZERO,
    val salePrice: BigDecimal = BigDecimal.ZERO,
    val stock: Int,
    val brandId: Long?,
    val categoryId: Long?,
    val images: List<ImageMetadata>? = listOf(),
    val attributes: List<ProductAttributeDto>? = listOf(),
    val isActive: Boolean = false,
)

data class EditProductDto (
    val id: Long,
    val name: JsonNullable<String> = JsonNullable.undefined(),
    val sku: JsonNullable<String> = JsonNullable.undefined(),
    val description: JsonNullable<String?> = JsonNullable.undefined(),
    val price: JsonNullable<BigDecimal> = JsonNullable.undefined(),
    val salePrice: JsonNullable<BigDecimal> = JsonNullable.undefined(),
    val stock: JsonNullable<Int> = JsonNullable.undefined(),
    val brandId: JsonNullable<Long?> = JsonNullable.undefined(),
    val categoryId: JsonNullable<Long?> = JsonNullable.undefined(),
    val images: JsonNullable<List<EditImage>?> = JsonNullable.undefined(),
    val attributes: JsonNullable<List<ProductAttributeDto>?> = JsonNullable.undefined(),
    val isActive: JsonNullable<Boolean> = JsonNullable.undefined(),
)

data class ProductBrandCount(
    val id: Long,
    val name: String,
    val count: Long
)

data class ProductQueryParams(
    val categoryId: Long?,
    val brands: List<Long> = emptyList(),
    val attributes: List<ProductAttributeDto>? = emptyList(),
    val page: Int = PageConstants.DEFAULT_PAGE,
    val pageSize: Int = PageConstants.DEFAULT_PAGE_SIZE,
    val sort: SortOrder = SortOrder.TOP,
    val price: RangeData? = null
)

data class ProductCategoryDto(
    val id: Long,
)