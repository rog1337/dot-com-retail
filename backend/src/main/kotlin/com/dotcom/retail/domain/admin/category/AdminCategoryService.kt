package com.dotcom.retail.domain.admin.category

import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.category.CategoryRepository
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttribute
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminCategoryService(
    private val categoryRepository: CategoryRepository,
    private val categoryAttributeRepository: CategoryAttributeRepository
) {
    fun search(query: String, page: Int, size: Int): Page<Category> {
        val pageable = PageRequest.of(page, size)
        return categoryRepository.findByNameContainingIgnoreCase(query, pageable)
    }

    fun searchAttribute(query: String, page: Int, size: Int): Page<CategoryAttribute> {
        val pageable = PageRequest.of(page, size)
        return categoryAttributeRepository.searchByAttributeOrLabel(query, pageable)
    }
}