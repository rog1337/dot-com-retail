package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.constants.ApiRoutes.Product
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(Product.BASE)
class ProductController(
    private val productService: ProductService,
    private val productMapper: ProductMapper
) {

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ProductDto> {
        val product = productService.get(id)
        return ResponseEntity.ok(productMapper.toDto(product))
    }

    @GetMapping("${Product.SLUG}/{slug}")
    fun getBySlug(@PathVariable slug: String): ResponseEntity<ProductDto> {
        val product = productService.getBySlug(slug)
        return ResponseEntity.ok(productMapper.toDto(product))
    }

    @PostMapping
    fun create(
        @RequestPart("product") product: CreateProduct,
        @RequestPart("images") imageFiles: List<MultipartFile>,
    ): ResponseEntity<ProductDto> {
        val product = productService.create(product, imageFiles)
        return ResponseEntity<ProductDto>(productMapper.toDto(product), HttpStatus.CREATED)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun edit(
        @PathVariable id: Long,
        @RequestPart("product") dto: EditProductDto,
        @RequestPart("images") imageFiles: List<MultipartFile>,
    ): ResponseEntity<ProductDto> {
        val product = productService.edit(id, dto, imageFiles)
        return ResponseEntity<ProductDto>(productMapper.toDto(product), HttpStatus.OK)
    }

    @GetMapping("{productId}${Product.IMAGE}/{imageId}")
    fun getImage(@PathVariable productId: Long, @PathVariable imageId: Long): ResponseEntity<Resource> {
        val image = productService.getImage(productId, imageId)
        return ResponseEntity<Resource>.ok().contentType(MediaType.IMAGE_JPEG).body(image)
    }
}