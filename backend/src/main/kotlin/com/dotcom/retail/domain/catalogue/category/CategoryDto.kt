package com.dotcom.retail.domain.catalogue.category

import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeDto

data class CategoryDto(
    val id: Long,
    val name: String,
    val attributes: List<CategoryAttributeDto>,
    val childrenIds: List<Long>?,
    val parentId: Long?
)

data class CreateCategoryRequest(
    val name: String,
    val attributeIds: List<Long>? = null,
    val parentId: Long?,
)

data class EditCategoryRequest(
    val id: Long,
    val name: String,
    val attributeIds: List<Long>? = null,
    val parentId: Long?,
)