package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.constants.ApiRoutes

fun Image.toDto(): ImageDto = ImageDto(
    id = id,
    url = "${ApiRoutes.Image.BASE}/$id",
    sortOrder = sortOrder,
)