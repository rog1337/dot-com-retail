package com.dotcom.retail.domain.admin.brand

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.admin.brand.dto.AdminBrandDto
import com.dotcom.retail.domain.admin.brand.dto.CreateBrand
import com.dotcom.retail.domain.admin.brand.dto.EditBrand
import com.dotcom.retail.domain.catalogue.brand.BrandMapper
import com.dotcom.retail.domain.catalogue.brand.BrandService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiRoutes.Admin.Brand.BASE)
class AdminBrandController(
    private val brandService: BrandService,
    private val brandMapper: BrandMapper
) {
    @PostMapping
    fun create(@RequestBody brand: CreateBrand): ResponseEntity<AdminBrandDto> {
        val brand = brandService.create(brand)
        return status(HttpStatus.CREATED).body(brandMapper.toAdminDto(brand))
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: Long): ResponseEntity<AdminBrandDto> {
        val brand = brandService.get(id)
        return ok(brandMapper.toAdminDto(brand))
    }

    @PutMapping("{id}")
    fun edit(@RequestBody brand: EditBrand): ResponseEntity<AdminBrandDto> {
        val brand = brandService.edit(brand)
        return ok(brandMapper.toAdminDto(brand))
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        brandService.delete(id)
        return ResponseEntity.noContent().build()
    }
}