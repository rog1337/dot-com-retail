package com.dotcom.retail.domain.catalogue.category

import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeMapper
import org.springframework.stereotype.Component

@Component
class CategoryMapper(private val categoryAttributeMapper: CategoryAttributeMapper) {
    fun toDto(category: Category): CategoryDto = CategoryDto(
        id = category.id,
        name = category.name,
        attributes = category.attributes.map { categoryAttributeMapper.toDto(it) },
        childrenIds = category.children.map { it.id },
        parentId = category.parent?.id
    )
}
