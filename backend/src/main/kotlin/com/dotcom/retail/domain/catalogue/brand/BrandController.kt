package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.common.constants.ApiRoutes.Brand
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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
        return ResponseEntity.ok(brandMapper.toDto(brand))
    }

    @PostMapping
    fun create(@RequestBody brand: CreateBrand): ResponseEntity<BrandDto> {
        val brand = brandService.create(brand)
        return ResponseEntity<BrandDto>(brandMapper.toDto(brand), HttpStatus.CREATED)
    }

    @PutMapping("{id}")
    fun edit(@RequestBody brand: EditBrand): ResponseEntity<BrandDto> {
        val brand = brandService.edit(brand)
        return ResponseEntity.ok(brandMapper.toDto(brand))
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> {
        brandService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("{id}" + Brand.IMAGE)
    fun getImage(@PathVariable id: Long): ResponseEntity<Resource> {
        val imageFile = brandService.getImage(id)
        return ResponseEntity<Resource>.ok().contentType(MediaType.IMAGE_JPEG).body(imageFile)
    }
}
