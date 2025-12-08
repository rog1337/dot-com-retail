package com.dotcom.retail.domain.catalogue.category

import com.dotcom.retail.common.exception.catalogue.BrandNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {

    fun find(id: Long): Category? {
        return categoryRepository.findByIdOrNull(id)
    }

    fun get(id: Long): Category {
        return categoryRepository.findById(id).orElseThrow { BrandNotFoundException(id) }
    }
}