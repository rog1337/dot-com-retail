package com.dotcom.retail.domain.admin.category.dto

import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.category.attribute.FilterType

data class CreateCategoryAttribute(
    val attribute: String,
    val label: String,
    val unit: String? = null,
    val dataType: AttributeDataType = AttributeDataType.TEXT,
    val filterType: FilterType = FilterType.CHECKBOX,
    val displayOrder: Int = 0,
    val isPublic: Boolean = false,
    val categories: List<Long>? = null
)

data class EditCategoryAttribute(
    val id: Long,
    val attribute: String,
    val label: String,
    val unit: String?,
    val dataType: AttributeDataType = AttributeDataType.TEXT,
    val filterType: FilterType = FilterType.CHECKBOX,
    val displayOrder: Int,
    val isPublic: Boolean,
    val categories: List<Long>?
)