package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.common.constants.ApiRoutes.Brand
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Brand.BASE)
class BrandController(
    private val brandService: BrandService,
    private val brandMapper: BrandMapper
) {
    @GetMapping("{id}")
    fun get(@PathVariable id: Long): ResponseEntity<BrandDto> {
        val brand = brandService.get(id)
        return ok(brandMapper.toDto(brand))
    }
}
