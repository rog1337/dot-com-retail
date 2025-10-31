package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.image.ProductImage
import com.dotcom.retail.domain.catalogue.image.ProductImageDto

fun Product.toDto(): ProductDto = ProductDto(
    serverId = id,
    productId = productId,
    slug = slug,
    name = name,
    productDescription = productDescription,
    storeDescription = storeDescription,
    price = price,
    stock = stock,
    brand = brand,
    category = category,
    images = images.map { it.toDto() },
    attributes = attributes.map { it.toDto() },
    dimensions = dimensions,
    weightKg = weightKg,
    weightLbs = weightLbs,
    listed = listed,
)

fun ProductAttribute.toDto(): ProductAttributeDto {
    val value = listOf(valueNumber, valueString, valueBoolean).firstNotNullOfOrNull { it }
    return ProductAttributeDto(
        name = name,
        value = value?.toString(),
        unit = unit,
    )
}

fun ProductAttributeDto.toEntity(): ProductAttribute {
    return ProductAttribute(
        name = name ?: "",
        valueString = value as? String,
        valueNumber = (value as? Number)?.toDouble(),
        valueBoolean = value as? Boolean,
        unit = unit,
    )
}

fun ProductImage.toDto(): ProductImageDto = ProductImageDto(
    url = url
)

//fun CreateProductDto.toEntity(): Product = Product(
fun CreateProductDto.toEntity(): Product {
    val attrs = attributes?.map { it.toEntity() }
    return Product(productId = productId,
    name = name,
    slug = "",
    productDescription = productDescription,
    storeDescription = storeDescription,
    price = price,
    stock = stock,
    brand = brand,
    category = category,
//    images = images,
    attributes = attrs?.toMutableList() ?: mutableListOf(),
    dimensions = dimensions,
    weightKg = weightKg,
    listed = listed,)
}