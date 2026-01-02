package com.dotcom.retail.domain.catalogue.category

import org.springframework.stereotype.Component

@Component
class CategoryMapper {
    fun toDto(category: Category): CategoryDto = CategoryDto(
        id = category.id,
        name = category.name,
    )
}
