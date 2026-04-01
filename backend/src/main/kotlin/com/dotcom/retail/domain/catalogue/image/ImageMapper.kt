package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.config.properties.AppProperties
import com.dotcom.retail.config.properties.FileProperties
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.file.Path

@Component
class ImageMapper(
    private val appProperties: AppProperties,
    private val fileProperties: FileProperties,
) {

    fun toImageDto(image: Image, path: Path): ImageDto {
        return ImageDto(
            id = image.id,
            sortOrder = image.sortOrder,
            altText = image.altText,
            urls = urlForSize(image, path),
        )
    }

    private fun urlForSize(image: Image, path: Path): ImageUrls {
        return ImageUrls(
            sm = toImageUrl(path.resolve(image.fileNameForSize(ImageSize.THUMBNAIL))),
            md = toImageUrl(path.resolve(image.fileNameForSize(ImageSize.MEDIUM))),
            lg = toImageUrl(path.resolve(image.fileNameForSize(ImageSize.FULL))),
        )
    }

    private fun toImageUrl(path: Path): String {
        return URI.create(appProperties.url + "/" + path).toString()
    }

    fun toProductImageDto(image: Image): ImageDto {
        val path = fileProperties.imagesPath.resolve(fileProperties.productPath)
        return toImageDto(image, path)
    }

    fun toBrandImageDto(image: Image): ImageDto {
        val path = fileProperties.imagesPath.resolve(fileProperties.brandPath)
        return toImageDto(image, path)
    }
}