package com.dotcom.retail.domain.catalogue.image

enum class ImageSize(val suffix: String, val width: Int, val height: Int) {
    THUMBNAIL("sm", 120, 120),
    MEDIUM("md", 600, 600),
    FULL("lg", 1200, 1200);
}