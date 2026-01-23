package com.dotcom.retail.domain.catalogue.filter

import com.dotcom.retail.domain.catalogue.category.CategoryDto
import com.dotcom.retail.domain.catalogue.category.attribute.FilterType
import com.dotcom.retail.domain.catalogue.product.ProductAttributeValueCount
import com.dotcom.retail.domain.catalogue.product.ProductBrandCount

data class Filter(
    val category: Long,
    val attributes: List<FilterAttribute>,
    val brands: List<ProductBrandCount>,
//    val categories: List<CategoryDto>
)

data class FilterAttribute(
    val id: Long,
    val attribute: String,
    val label: String,
    val type: FilterType,
    val displayOrder: Int,
    val isPublic: Boolean,
    val values: List<ProductAttributeValueCount>
)
