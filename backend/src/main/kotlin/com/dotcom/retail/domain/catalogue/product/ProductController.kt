package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.constants.ApiRoutes.Product
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Product.BASE)
class ProductController(
    private val productService: ProductService
) {

    @GetMapping("{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductDto> {
        val product = productService.get(id)
        return ResponseEntity.ok(product.toDto())
    }

    @GetMapping("${Product.SLUG}/{slug}")
    fun getProductBySlug(@PathVariable slug: String): ResponseEntity<ProductDto> {
        val product = productService.getBySlug(slug)
        return ResponseEntity.ok(product.toDto())
    }

    @PostMapping
    fun createProduct(@RequestBody dto: CreateProductDto): ResponseEntity<ProductDto> {
        val product = productService.create(dto)
        return ResponseEntity<ProductDto>(product.toDto(), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun edit(@PathVariable id: Long, @RequestBody dto: EditProductDto): ResponseEntity<ProductDto> {
        val product = productService.edit(id, dto)
        return ResponseEntity<ProductDto>(product.toDto(), HttpStatus.OK)
    }

    @GetMapping("{productId}${Product.IMAGE}/{imageId}")
    fun getImage(@PathVariable productId: Long, @PathVariable imageId: Long): ResponseEntity<Resource> {
        val image = productService.getImage(productId, imageId)
        return ResponseEntity<Resource>.ok().contentType(MediaType.IMAGE_JPEG).body(image)
    }
}