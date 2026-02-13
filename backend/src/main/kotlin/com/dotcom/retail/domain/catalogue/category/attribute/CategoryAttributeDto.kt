package com.dotcom.retail.domain.catalogue.category.attribute

data class CategoryAttributeDto(
    val id: Long,
    val attribute: String,
    val label: String,
    val unit: String?,
    val dataType: AttributeDataType,
    val filterType: FilterType,
    val displayOrder: Int,
    val isPublic: Boolean,
    val categories: List<Long>?
)

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