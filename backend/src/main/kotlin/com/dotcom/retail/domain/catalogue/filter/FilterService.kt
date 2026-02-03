package com.dotcom.retail.domain.catalogue.filter;

import com.dotcom.retail.domain.catalogue.category.CategoryRepository
import com.dotcom.retail.domain.catalogue.category.CategoryService
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeRepository
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import com.dotcom.retail.domain.catalogue.product.ProductService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service;

@Service
class FilterService(
    private val categoryService: CategoryService,
    private val productService: ProductService,
    private val categoryAttributeRepository: CategoryAttributeRepository
) {

    fun getPublicFilters(categoryId: Long): Filter {
        val category = categoryService.get(categoryId)
        val categoryAttributes = category.attributes

        val attributes = categoryAttributes.filter { it.isPublic }.map { attr ->
            val attrKey = attr.attribute
            val valueCount = productService.findAttributeCounts(categoryId, attrKey)
            val values: List<ValueCount> = valueCount.map {
                val value = when(attr.dataType) {
                    AttributeDataType.NUMBER -> it.value.toString().toBigDecimal()
                    AttributeDataType.BOOLEAN -> it.value.toString().toBoolean()
                    else -> it.value
                }
                ValueCount(value = value, count = it.count)
            }
            FilterAttribute(
                id = attr.id,
                attribute = attrKey,
                label = attr.label,
                filterType = attr.filterType,
                displayOrder = attr.displayOrder,
                values = values,
            )
        }
        return Filter(
            categoryId = categoryId,
            attributes = attributes,
            brands = productService.getBrandCounts(categoryId),
        )
    }
}
