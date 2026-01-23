package com.dotcom.retail.domain.catalogue.category

import com.dotcom.retail.common.exception.CategoryAlreadyExistsException
import com.dotcom.retail.common.exception.CategoryNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {

    fun find(id: Long): Category? {
        return categoryRepository.findByIdOrNull(id)
    }

    fun get(id: Long): Category {
        return categoryRepository.findById(id).orElseThrow { CategoryNotFoundException(id) }
    }

    fun existsByName(name: String): Boolean {
        return categoryRepository.existsByName(name)
    }

    fun save(category: Category): Category {
        return categoryRepository.save(category)
    }

    fun create(data: CreateCategoryRequest): Category {
        if (existsByName(data.name)) throw CategoryAlreadyExistsException(data.name)

        val parent = data.parentId?.let { get(it) }

        val category = Category(
            name = data.name,
            parent = parent,
        )

        if (parent != null) {
            parent.children.add(category)
            return save(parent)
        }

        return save(category)
    }
}