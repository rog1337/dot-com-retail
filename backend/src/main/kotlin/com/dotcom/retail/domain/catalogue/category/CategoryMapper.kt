package com.dotcom.retail.domain.catalogue.category

import com.dotcom.retail.domain.admin.category.dto.AdminAttributeDto
import com.dotcom.retail.domain.admin.category.dto.AdminCategoryAttributeDto
import com.dotcom.retail.domain.admin.category.dto.AdminCategoryDto
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttribute
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeMapper
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class CategoryMapper(private val categoryAttributeMapper: CategoryAttributeMapper) {
    fun toDto(category: Category): CategoryDto = CategoryDto(
        id = category.id,
        name = category.name,
        attributes = category.attributes.map { categoryAttributeMapper.toDto(it) },
    )

    fun toAdminDto(category: Category): AdminCategoryDto = AdminCategoryDto(
        id = category.id,
        name = category.name,
        attributes = category.attributes.map { toAdminAttributeDto(it) },
    )

    private fun toAdminAttributeDto(a: CategoryAttribute): AdminAttributeDto = AdminAttributeDto(
        id = a.id,
        attribute = a.attribute,
        label = a.label,
        unit = a.unit,
        dataType = a.dataType,
        filterType = a.filterType,
        displayOrder = a.displayOrder,
    )

    fun toPagedAdminDto(categories: Page<Category>): Page<AdminCategoryDto> =
        categories.map { toAdminDto(it) }
}
