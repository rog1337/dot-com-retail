package com.dotcom.retail.domain.admin.category

import com.dotcom.retail.common.constants.ApiRoutes.Admin
import com.dotcom.retail.domain.admin.category.dto.*
import com.dotcom.retail.domain.catalogue.category.CategoryMapper
import com.dotcom.retail.domain.catalogue.category.CategoryService
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeDto
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeMapper
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Admin.Category.BASE)
class AdminCategoryController(
    private val categoryMapper: CategoryMapper,
    private val categoryService: CategoryService,
    private val categoryAttributeService: CategoryAttributeService,
    private val categoryAttributeMapper: CategoryAttributeMapper
) {
    @PostMapping
    fun create(@RequestBody data: CreateCategoryRequest): ResponseEntity<AdminCategoryDto> {
        val category = categoryService.create(data)
        return status(HttpStatus.CREATED).body(categoryMapper.toAdminDto(category))
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<AdminCategoryDto> {
        val category = categoryService.get(id)
        return ok(categoryMapper.toAdminDto(category))
    }

    @PutMapping("{id}")
    fun edit(@PathVariable id: Long, @RequestBody data: EditCategoryRequest): ResponseEntity<AdminCategoryDto> {
        val category = categoryService.edit(id, data)
        return ok(categoryMapper.toAdminDto(category))
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        categoryService.delete(id)
        return noContent().build()
    }

    @PostMapping(Admin.Category.ATTRIBUTE)
    fun createAttribute(@RequestBody data: CreateCategoryAttribute): ResponseEntity<CategoryAttributeDto> {
        val attribute = categoryAttributeService.create(data)
        return status(HttpStatus.CREATED).body(categoryAttributeMapper.toAdminDto(attribute))
    }

    @PutMapping(Admin.Category.ATTRIBUTE + "{id}")
    fun editAttribute(@RequestBody data: EditCategoryAttribute): ResponseEntity<CategoryAttributeDto> {
        val attribute = categoryAttributeService.edit(data)
        return ok().body(categoryAttributeMapper.toDto(attribute))
    }

    @DeleteMapping(Admin.Category.ATTRIBUTE + "{id}")
    fun deleteAttribute(@PathVariable id: Long): ResponseEntity<Void> {
        categoryAttributeService.delete(id)
        return noContent().build()
    }
}