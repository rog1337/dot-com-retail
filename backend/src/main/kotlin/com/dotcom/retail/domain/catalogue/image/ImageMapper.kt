package com.dotcom.retail.domain.catalogue.image

fun Image.toDto(): ImageDto = ImageDto(
    id = id,
    url = url
)