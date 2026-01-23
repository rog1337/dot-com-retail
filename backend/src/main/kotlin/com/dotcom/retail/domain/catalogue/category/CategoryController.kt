package com.dotcom.retail.domain.catalogue.category

import com.dotcom.retail.common.constants.ApiRoutes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiRoutes.Category.BASE)
class CategoryController(private val categoryService: CategoryService, private val categoryMapper: CategoryMapper) {

    @GetMapping("{id}")
    fun get(@PathVariable id: Long): ResponseEntity<CategoryDto> {
        val category = categoryService.get(id)
        return ResponseEntity.ok(categoryMapper.toDto(category))
    }

    @PostMapping
    fun create(@RequestBody data: CreateCategoryRequest): ResponseEntity<CategoryDto> {
        val category = categoryService.create(data)
        return ResponseEntity<CategoryDto>(categoryMapper.toDto(category), HttpStatus.CREATED)
    }
}