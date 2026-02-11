package com.dotcom.retail.domain.catalogue.filter

import com.dotcom.retail.domain.catalogue.product.ProductBrandCount
import org.springframework.stereotype.Component

@Component
class FilterMapper {
    fun toDto(
        categoryId: Long,
        attributes: List<FilterAttribute>,
        brands: List<ProductBrandCount>,
        price: RangeData,
    ): Filter {
        return Filter(
            categoryId = categoryId,
            attributes = attributes,
            brands = brands,
            price = price
        )
    }

    fun toRangeData(min: Double, max: Double): RangeData {
        return RangeData(min, max)
    }
}
