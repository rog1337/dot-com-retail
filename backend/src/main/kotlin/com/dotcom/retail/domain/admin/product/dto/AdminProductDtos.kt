package com.dotcom.retail.domain.admin.product.dto

import com.dotcom.retail.domain.catalogue.brand.BrandDto
import com.dotcom.retail.domain.catalogue.image.ImageDto
import com.dotcom.retail.domain.catalogue.product.ProductAttributeDto
import com.dotcom.retail.domain.catalogue.product.ProductCategoryDto
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
    val category: ProductCategoryDto?,
    val attributes: List<ProductAttributeDto>?,
    val images: List<ImageDto>?,
    val reviewCount: Int?,
    val averageRating: Double?,
    val isActive: Boolean,
)