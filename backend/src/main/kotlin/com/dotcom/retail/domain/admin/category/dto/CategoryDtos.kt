package com.dotcom.retail.domain.admin.category.dto

import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.category.attribute.FilterType

data class AdminCategoryDto(
    val id: Long,
    val name: String,
    val attributes: List<AdminAttributeDto>,
)

data class AdminAttributeDto(
    val id: Long,
    val attribute: String,
    val label: String,
    val unit: String?,
    val dataType: AttributeDataType,
    val filterType: FilterType,
    val displayOrder: Int,
)

data class CreateCategoryRequest(
    val name: String,
    val attributeIds: List<Long>? = null,
)

data class EditCategoryRequest(
    val id: Long,
    val name: String,
    val attributeIds: List<Long>? = null,
)