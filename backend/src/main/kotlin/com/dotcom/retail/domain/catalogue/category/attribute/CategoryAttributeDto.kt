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