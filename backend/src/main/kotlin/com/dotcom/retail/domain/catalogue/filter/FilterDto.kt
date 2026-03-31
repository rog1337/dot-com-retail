package com.dotcom.retail.domain.catalogue.filter

import com.dotcom.retail.domain.catalogue.category.attribute.FilterType
import com.dotcom.retail.domain.catalogue.product.ProductBrandCount

data class Filter(
    val category: FilterCategoryDto,
    val attributes: List<FilterAttribute>,
    val brands: List<ProductBrandCount>,
    val price: RangeData
)

data class FilterAttribute(
    val id: Long,
    val attribute: String,
    val label: String,
    val unit: String? = null,
    val filterType: FilterType,
    val displayOrder: Int,
    val values: List<FilterAttributeData>
)

data class FilterCategoryDto(
    val id: Long,
    val name: String
)

interface FilterAttributeData
data class RangeData(val min: Double, val max: Double) : FilterAttributeData
//data class OptionData(val options: List<OptionCount>) : FilterAttributeData
data class ValueCount(val value: Any, val count: Long): FilterAttributeData
