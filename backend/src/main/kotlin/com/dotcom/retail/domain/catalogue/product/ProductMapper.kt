package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.brand.toDto
import com.dotcom.retail.domain.catalogue.category.toDto
import com.dotcom.retail.domain.catalogue.image.toDto

fun Product.toDto(): ProductDto = ProductDto(
    id = id,
    name = name,
    sku = sku,
    slug = slug,
    description = description,
    price = price,
    salePrice = salePrice,
    stock = stock,
    brand = brand?.toDto(),
    category = category?.toDto(),
    attributes = attributes,
    images = images.map { image -> image.toDto() },
    isActive = isActive,
)