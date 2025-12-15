package com.dotcom.retail.domain.catalogue.category

fun Category.toDto(): CategoryDto = CategoryDto (
    id = id,
    name = name,
)