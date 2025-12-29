package com.dotcom.retail.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "file")
data class FileProperties (
    val dir: String,
    val image: ImageProperties,
)

data class ImageProperties(
    val dir: String,
    val product: ProductProperties,
    val brand: BrandProperties,
)

data class ProductProperties(val dir: String)
data class BrandProperties(val dir: String)