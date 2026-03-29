package com.dotcom.retail.domain.admin.product.dto

import com.dotcom.retail.domain.catalogue.brand.BrandDto
import com.dotcom.retail.domain.catalogue.image.EditImage
import com.dotcom.retail.domain.catalogue.image.ImageDto
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import com.dotcom.retail.domain.catalogue.product.ProductAttributeDto
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.openapitools.jackson.nullable.JsonNullable
import java.math.BigDecimal

data class AdminProductDto(
    val id: Long,
    val name: String,
    val description: String?,
    val sku: String,
    val price: BigDecimal,
    val salePrice: BigDecimal,
    val stock: Int,
    val brand: BrandDto?,
    val category: AdminProductCategoryDto?,
    val attributes: List<ProductAttributeDto>?,
    val images: List<ImageDto>?,
    val reviewCount: Int?,
    val averageRating: Double?,
    val isActive: Boolean,
)

data class CreateProduct (
    @field:NotBlank(message = "Product name cannot be blank")
    val name: String,
    @field:NotBlank(message = "Product sku cannot be blank")
    val sku: String,
    val description: String?,
    @field:Min(value = 0)
    val price: BigDecimal = BigDecimal.ZERO,
    @field:Min(value = 0)
    val salePrice: BigDecimal = BigDecimal.ZERO,
    @field:Min(value = 0)
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

data class AdminProductCategoryDto(
    val id: Long,
    val name: String,
)