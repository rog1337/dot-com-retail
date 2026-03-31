package com.dotcom.retail.domain.catalogue.filter

import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.product.ProductBrandCount
import org.springframework.stereotype.Component

@Component
class FilterMapper {
    fun toDto(
        category: Category,
        attributes: List<FilterAttribute>,
        brands: List<ProductBrandCount>,
        price: RangeData,
    ): Filter {
        return Filter(
            category = toFilterCategoryDto(category),
            attributes = attributes,
            brands = brands,
            price = price
        )
    }

    fun toFilterCategoryDto(category: Category): FilterCategoryDto = FilterCategoryDto(
        id = category.id,
        name = category.name,
    )

    fun toRangeData(min: Double, max: Double): RangeData {
        return RangeData(min, max)
    }
}
