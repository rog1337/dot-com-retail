package com.dotcom.retail.domain.catalogue.category

data class CategoryDto(
    val id: Long,
    val name: String,
)

data class CreateCategoryRequest(
    val name: String,
    val parentId: Long?,
)