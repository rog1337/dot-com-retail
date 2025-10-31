package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.brand.Brand
import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.dimension.Dimension
import com.dotcom.retail.domain.catalogue.image.ProductImageDto
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class ProductDto (
    val serverId: Long,
    val productId: String?,
    val slug: String,
    val name: String,
    val productDescription: String?,
    val storeDescription: String?,
    var price: BigDecimal?,
    var stock: Int = 0,
    var brand: Brand?,
    var category: Category?,
    var images: List<ProductImageDto>,
    var attributes: List<ProductAttributeDto>,
    var dimensions: Dimension?,
    var weightKg: Double?,
    var weightLbs: Double?,
    var listed: Boolean = false,
)

data class ProductAttributeDto(
    val name: String?,
    val value: Any?,
    val unit: String?
)

data class CreateProductDto (
    val productId: String? = null,

    @field:NotBlank(message = "Product name cannot be blank")
    val name: String,

    val productDescription: String? = null,
    val storeDescription: String? = null,
    var price: BigDecimal? = BigDecimal.ZERO,
    var stock: Int = 0,
    var brand: Brand? = null,
    var category: Category? = null,
//    var images: List<ProductImageDto>, // TODO
    var attributes: List<ProductAttributeDto>?,
    var dimensions: Dimension?,
    var weightKg: Double?,
    var listed: Boolean = false,
)