package com.dotcom.retail.domain.catalogue.image

import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

data class ImageDto(
    val id: Long,
    val url: String,
    val sortOrder: Int,
)

data class CreateImage(
    val sortOrder: Int = 0,
)

data class EditImage(
    val id: Long,
    val sortOrder: Int,
)

data class ImageDeletionEvent(
    val filePaths: List<String>,
)

data class ImageStoringEvent(
    val file: MultipartFile,
    val filePath: Path,
)

data class ImageMetadata(
    val fileName: String,
    val sortOrder: Int = 0,
    val altText: String? = null,
)