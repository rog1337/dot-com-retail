package com.dotcom.retail.domain.catalogue.category.attribute

import org.springframework.stereotype.Service

@Service
class CategoryAttributeService(
    private val categoryAttributeRepository: CategoryAttributeRepository
) {
    fun save(it: CategoryAttribute): CategoryAttribute {
        return categoryAttributeRepository.save(it)
    }

    fun findAll(): List<CategoryAttribute> {
        return categoryAttributeRepository.findAll()
    }
}
