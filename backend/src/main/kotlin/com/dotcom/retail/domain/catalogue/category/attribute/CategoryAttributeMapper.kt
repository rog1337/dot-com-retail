package com.dotcom.retail.domain.catalogue.category.attribute

import com.dotcom.retail.domain.admin.category.dto.AdminCategoryAttributeDto
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class CategoryAttributeMapper {
    fun toDto(categoryAttribute: CategoryAttribute): CategoryAttributeDto = CategoryAttributeDto(
        id = categoryAttribute.id,
        attribute = categoryAttribute.attribute,
        label = categoryAttribute.label,
        unit = categoryAttribute.unit,
        filterType = categoryAttribute.filterType,
        dataType = categoryAttribute.dataType,
        displayOrder = categoryAttribute.displayOrder,
        isPublic = categoryAttribute.isPublic,
        categories = categoryAttribute.categories.map { it.id }
    )

    fun toAdminDto(categoryAttribute: CategoryAttribute): AdminCategoryAttributeDto = AdminCategoryAttributeDto(
        id = categoryAttribute.id,
        attribute = categoryAttribute.attribute,
        label = categoryAttribute.label,
        unit = categoryAttribute.unit,
        filterType = categoryAttribute.filterType,
        dataType = categoryAttribute.dataType,
        displayOrder = categoryAttribute.displayOrder,
        isPublic = categoryAttribute.isPublic,
        categories = categoryAttribute.categories.map { it.id }
    )

    fun toPagedAdminDto(page: Page<CategoryAttribute>): Page<AdminCategoryAttributeDto> =
        page.map { toAdminDto(it) }
}