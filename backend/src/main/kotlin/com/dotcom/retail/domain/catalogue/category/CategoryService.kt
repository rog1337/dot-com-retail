package com.dotcom.retail.domain.catalogue.category

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.CategoryAttributeError
import com.dotcom.retail.common.exception.CategoryError
import com.dotcom.retail.domain.admin.category.dto.CreateCategoryRequest
import com.dotcom.retail.domain.admin.category.dto.EditCategoryRequest
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val categoryAttributeRepository: CategoryAttributeRepository
) {

    fun find(id: Long): Category? {
        return categoryRepository.findByIdOrNull(id)
    }

    fun findAll(): List<Category> {
        return categoryRepository.findAll()
    }

    fun findAllById(ids: List<Long>): List<Category> {
        return categoryRepository.findAllById(ids)
    }

    fun get(id: Long): Category {
        return categoryRepository.findById(id).orElseThrow { AppException(CategoryError.CATEGORY_NOT_FOUND.withIdentifier(id)) }
    }

    fun existsByName(name: String): Boolean {
        return categoryRepository.existsByName(name)
    }

    fun save(category: Category): Category {
        return categoryRepository.save(category)
    }

    fun saveAll(it: List<Category>): List<Category> {
        return categoryRepository.saveAll(it)
    }

    @Transactional
    fun create(data: CreateCategoryRequest): Category {
        if (existsByName(data.name)) throw AppException(CategoryError.CATEGORY_ALREADY_EXISTS.withIdentifier(data.name))

        val parent = data.parentId?.let { get(it) }

        val attributes = data.attributeIds?.map {
            categoryAttributeRepository.findByIdOrNull(it) ?: throw AppException(CategoryAttributeError.CATEGORY_ATTRIBUTE_NOT_FOUND.withIdentifier(it))
        }?.toMutableList() ?: mutableListOf()

        val category = Category(
            name = data.name,
            parent = parent,
        )
        category.attributes.addAll(attributes)

        if (parent != null) {
            parent.children.add(category)
            save(parent)
        }

        return save(category)
    }

    @Transactional
    fun edit(id: Long, data: EditCategoryRequest): Category {
        val category = get(id)
        val parent = data.parentId?.let { get(data.parentId) }
        val attributes = data.attributeIds?.map {
            categoryAttributeRepository.findByIdOrNull(it) ?: throw AppException(CategoryAttributeError.CATEGORY_ATTRIBUTE_NOT_FOUND.withIdentifier(it))
        }?.toMutableList() ?: mutableListOf()

        category.apply {
            name = data.name
            this.attributes.addAll(attributes)
            this.parent = parent
        }

        parent?.let {
            it.children.add(category)
            save(it)
        }

        return save(category)
    }

    @Transactional
    fun delete(id: Long) {
        val category = get(id)
        categoryRepository.delete(category)
    }
}