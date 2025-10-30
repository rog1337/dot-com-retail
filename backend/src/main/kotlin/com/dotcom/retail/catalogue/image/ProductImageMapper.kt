package com.dotcom.retail.catalogue.image

import com.dotcom.retail.catalogue.product.Product
import com.dotcom.retail.catalogue.product.ProductDto

fun ProductImage.toDto(): ProductImageDto = ProductImageDto(
    url = url
)