package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.config.properties.AppProperties
import com.dotcom.retail.config.properties.FileProperties
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.file.Paths

@Component
class ImageMapper(
    private val appProperties: AppProperties,
    private val fileProperties: FileProperties,
) {
    fun toProductImageDto(image: Image, productId: Long): ImageDto = ImageDto(
        id = image.id,
        url = URI.create(appProperties.url + "/" + fileProperties.imagesPath
            .resolve(fileProperties.productPath).resolve(image.fileName).toString()).toString(),
//        url = URI.create(.toString(),
//        "${appProperties.url}${fileProperties.imagesPath}/product/${image.fileName}",
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