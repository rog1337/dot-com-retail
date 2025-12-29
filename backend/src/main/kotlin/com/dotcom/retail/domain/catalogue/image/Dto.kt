package com.dotcom.retail.domain.catalogue.image

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