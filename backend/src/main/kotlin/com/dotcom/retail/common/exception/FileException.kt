package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus

open class FileException(
    message: String = "File processing failed",
    status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    cause: Throwable? = null,
) : AppException(message, status, cause)

open class FileIsEmptyException(
    resourceName: String,
    identifier: Any? = null,
) : FileException(
    if (identifier != null) "$resourceName is empty: $identifier" else "$resourceName is empty",
    status = HttpStatus.BAD_REQUEST,
)

data class ImageIsEmptyException(val id: Any? = null) : FileIsEmptyException("Image", id)
data class FileIsNotAnImageException(val id: Any? = null) : FileException("File is not an image${if (id != null) ": $id" else ""}", HttpStatus.BAD_REQUEST)