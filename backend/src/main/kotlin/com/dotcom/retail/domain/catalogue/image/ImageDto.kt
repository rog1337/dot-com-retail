package com.dotcom.retail.domain.catalogue.image

import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

open class ImageDto(
    val id: Long,
    val urls: ImageUrls,
    val sortOrder: Int,
    val altText: String?,
)

data class ImageUrls(
    val sm: String,
    val md: String,
    val lg: String,
)

data class EditImage(
    val id: Long,
    val sortOrder: Int,
    val altText: String?,
)

data class ImageDeletionEvent(
    val filePaths: List<Path>,
)

data class ImageStoringEvent(
    val file: MultipartFile,
    val filePath: Path,
)

open class ImageMetadata(
    val fileName: String,
    val sortOrder: Int = 0,
    val altText: String? = null,
)