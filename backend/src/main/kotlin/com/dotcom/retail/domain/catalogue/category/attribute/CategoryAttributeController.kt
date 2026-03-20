package com.dotcom.retail.domain.catalogue.category.attribute

import com.dotcom.retail.common.constants.ApiRoutes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiRoutes.Category.Attribute.BASE)
class CategoryAttributeController(
    private val categoryAttributeMapper: CategoryAttributeMapper,
    private val categoryAttributeService: CategoryAttributeService
) {

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<CategoryAttributeDto> {
        val attribute = categoryAttributeService.get(id)
        return ResponseEntity<CategoryAttributeDto>.ok().body(categoryAttributeMapper.toDto(attribute))
    }
}