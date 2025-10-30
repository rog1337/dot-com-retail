package com.dotcom.retail.user

import com.dotcom.retail.catalogue.image.ProductImage
import com.dotcom.retail.catalogue.image.ProductImageDto

fun User.toDto() = UserDto(
    id = id.toString(),
    email = email,
    displayName = displayName,
)