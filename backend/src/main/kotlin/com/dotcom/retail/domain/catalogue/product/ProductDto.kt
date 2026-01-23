package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.brand.BrandDto
import com.dotcom.retail.domain.catalogue.category.CategoryDto
import com.dotcom.retail.domain.catalogue.image.ImageDto
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import jakarta.validation.constraints.NotBlank
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

data class ProductDto (
    val id: Long,
    val name: String,
    val description: String?,
    val slug: String,
    val sku: String,
    val price: BigDecimal,
    val salePrice: BigDecimal,
    val stock: Int,
    val brand: BrandDto?,
    val category: CategoryDto?,
//    val attributes: Map<String, Any>?,
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
    val salePrice: BigDecimal = price,
    val stock: Int,
    val brandId: Long?,
    val categoryId: Long?,
    val images: List<ImageMetadata>?,
    val attributes: List<ProductAttributeDto>?,
    val isActive: Boolean = false,
)

data class EditProductDto (
    val id: Long,
    val name: String,
    val sku: String,
    val description: String?,
    val price: BigDecimal,
    val salePrice: BigDecimal,
    val stock: Int,
    val brandId: Long?,
    val categoryId: Long?,
    val images: List<ImageMetadata>?,
    val attributes: List<ProductAttributeDto>?,
    val isActive: Boolean = false,
)

data class ProductAttributeValueCount(
    val value: Any,
    val count: Long,
)

data class ProductBrandCount(
    val id: Long,
    val name: String,
    val count: Long
)

data class ProductQueryParams(
    val categoryId: Long,
    val brands: List<Long> = emptyList(),
    val attributes: List<ProductAttributeDto>? = emptyList(),
    val page: Int,
    val pageSize: Int,
)