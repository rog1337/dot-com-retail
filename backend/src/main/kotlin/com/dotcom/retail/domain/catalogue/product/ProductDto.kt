package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.brand.BrandDto
import com.dotcom.retail.domain.catalogue.category.CategoryDto
import com.dotcom.retail.domain.catalogue.image.ImageDto
import jakarta.validation.constraints.NotBlank
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
    val attributes: Map<String, Any>?,
    val images: List<ImageDto>?,
    val isActive: Boolean,
)

data class CreateProductDto (
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
    val images: List<Long>?,
    val attributes: Map<String, Any>?,
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
    val images: List<ImageDto>?,
    val attributes: Map<String, Any>?,
    val isActive: Boolean = false,
)