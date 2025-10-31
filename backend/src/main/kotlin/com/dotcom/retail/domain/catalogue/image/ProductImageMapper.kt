package com.dotcom.retail.domain.catalogue.image

fun ProductImage.toDto(): ProductImageDto = ProductImageDto(
    url = url
)