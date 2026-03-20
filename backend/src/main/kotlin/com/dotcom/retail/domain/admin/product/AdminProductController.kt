package com.dotcom.retail.domain.admin.product

import com.dotcom.retail.common.constants.ApiRoutes.Admin
import com.dotcom.retail.domain.admin.product.dto.AdminProductDto
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import com.dotcom.retail.domain.catalogue.product.CreateProduct
import com.dotcom.retail.domain.catalogue.product.EditProductDto
import com.dotcom.retail.domain.catalogue.product.ProductMapper
import com.dotcom.retail.domain.catalogue.product.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(Admin.Product.BASE)
class AdminProductController(
    private val productService: ProductService,
    private val productMapper: ProductMapper
) {
    @PostMapping
    fun create(
        @RequestPart("product") product: CreateProduct,
        @RequestPart("images") imageFiles: List<MultipartFile>?,
    ): ResponseEntity<AdminProductDto> {
        val product = productService.create(product, imageFiles)
        return status(HttpStatus.CREATED).body(productMapper.toAdminDto(product))
    }

    @GetMapping("{id}")
    fun getById(@PathVariable("id") id: Long): ResponseEntity<AdminProductDto> {
        val product = productService.get(id)
        return ok(productMapper.toAdminDto(product))
    }

    @PatchMapping("{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun update(
        @PathVariable id: Long,
        @RequestPart("product") data: EditProductDto,
        @RequestPart("images") imageFiles: List<MultipartFile>?,
        @RequestPart("image_metadata") imageMetadata: List<ImageMetadata>?
    ): ResponseEntity<AdminProductDto> {
        val product = productService.update(id, data, imageFiles, imageMetadata)
        return ok(productMapper.toAdminDto(product))
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        productService.delete(id)
        return noContent().build()
    }
}