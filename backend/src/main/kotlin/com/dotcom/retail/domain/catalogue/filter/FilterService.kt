package com.dotcom.retail.domain.catalogue.filter;

import com.dotcom.retail.domain.catalogue.category.CategoryService
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeService
import com.dotcom.retail.domain.catalogue.product.ProductService
import org.springframework.stereotype.Service;

@Service
class FilterService(
    private val categoryAttributeService: CategoryAttributeService,
    private val categoryService: CategoryService,
    private val productService: ProductService
) {

    fun getPublicFilters(categoryId: Long): Filter {
        val category = categoryService.get(categoryId)
        val categoryAttributes = category.attributes

        val attributes = categoryAttributes.filter { it.isPublic }.map { attr ->
            val attrKey = attr.attribute
            FilterAttribute(
                id = attr.id,
                attribute = attrKey,
                label = attr.label,
                type = attr.type,
                displayOrder = attr.displayOrder,
                isPublic = attr.isPublic,
                values = productService.getAttributeCounts(categoryId, attrKey),
            )
        }
        return Filter(
            category = categoryId,
            attributes = attributes,
            brands = productService.getBrandCounts(categoryId),
        )
    }
}
