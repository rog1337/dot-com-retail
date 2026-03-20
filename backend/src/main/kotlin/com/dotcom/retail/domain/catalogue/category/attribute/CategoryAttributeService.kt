package com.dotcom.retail.domain.catalogue.category.attribute

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.CategoryAttributeError
import com.dotcom.retail.domain.admin.category.dto.CreateCategoryAttribute
import com.dotcom.retail.domain.admin.category.dto.EditCategoryAttribute
import com.dotcom.retail.domain.catalogue.category.CategoryService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryAttributeService(
    private val categoryAttributeRepository: CategoryAttributeRepository,
    private val categoryService: CategoryService
) {

    fun get(id: Long): CategoryAttribute {
        return categoryAttributeRepository.findByIdOrNull(id) ?: throw AppException(CategoryAttributeError.CATEGORY_ATTRIBUTE_NOT_FOUND.withIdentifier(id))
    }

    fun save(it: CategoryAttribute): CategoryAttribute {
        return categoryAttributeRepository.save(it)
    }

    fun findByAttribute(name: String): List<CategoryAttribute> {
        return categoryAttributeRepository.findByAttribute(name)
    }

    fun findAll(): List<CategoryAttribute> {
        return categoryAttributeRepository.findAll()
    }

    @Transactional
    fun create(data: CreateCategoryAttribute): CategoryAttribute {
        if (findByAttribute(data.attribute).isNotEmpty()) throw AppException(CategoryAttributeError.CATEGORY_ATTRIBUTE_ALREADY_EXISTS.withIdentifier(data.attribute))
        val categories = data.categories?.map { categoryService.get(it) }?.toMutableSet() ?: mutableSetOf()

        val attribute = save(CategoryAttribute(
            attribute = data.attribute,
            label = data.label,
            unit = data.unit,
            dataType = data.dataType,
            filterType = data.filterType,
            displayOrder = data.displayOrder,
            isPublic = data.isPublic,
            categories = categories
        ))

        categories.forEach { it.attributes.add(attribute) }
        categoryService.saveAll(categories.toList())

        return save(attribute)
    }

    fun delete(id: Long) {
        val attribute = get(id)
        categoryAttributeRepository.delete(attribute)
    }

    fun edit(data: EditCategoryAttribute): CategoryAttribute {
        val categoryAttribute = get(data.id)
        val categories = data.categories?.map { categoryService.get(it) }?.toMutableSet() ?: mutableSetOf()

        categoryAttribute.apply {
            attribute = data.attribute
            label = data.label
            unit = data.unit
            dataType = data.dataType
            filterType = data.filterType
            displayOrder = data.displayOrder
            isPublic = data.isPublic
            this.categories = categories
        }

        save(categoryAttribute)
        categories.forEach { it.attributes.add(categoryAttribute) }
        categoryService.saveAll(categories.toList())
        return categoryAttribute
    }
}
