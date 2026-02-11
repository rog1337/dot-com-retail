package com.dotcom.retail.domain.catalogue.filter

import com.dotcom.retail.common.constants.ApiRoutes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiRoutes.Filter.BASE)
class FilterController(private val filterService: FilterService) {

    @GetMapping
    fun getFilters(
        @RequestParam(value = "categoryId") categoryId: Long,
    ): ResponseEntity<Filter> {
        val filters = filterService.getFilters(categoryId)
        return ResponseEntity.ok(filters)
    }
}