package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.config.properties.AppProperties
import org.springframework.stereotype.Component

@Component
class ImageMapper(
    private val appProperties: AppProperties,
) {
    fun toProductImageDto(image: Image, productId: Long): ImageDto = ImageDto(
        id = image.id,
        url = "${appProperties.url}${ApiRoutes.Product.BASE}/$productId${ApiRoutes.Product.IMAGE}/${image.id}",
        sortOrder = image.sortOrder,
        altText = image.altText
    )

    fun toBrandImageDto(image: Image, brandId: Long): ImageDto = ImageDto(
        id = image.id,
        url = "${appProperties.url}${ApiRoutes.Brand.BASE}/$brandId${ApiRoutes.Brand.IMAGE}/${image.id}",
        sortOrder = image.sortOrder,
        altText = image.altText
    )
}