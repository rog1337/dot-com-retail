package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.constants.ApiRoutes.Product
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Product.BASE)
class ProductController(
    private val productService: ProductService
) {

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductDto> {
        val product = productService.get(id)
        return ResponseEntity.ok(product.toDto())
    }

    @GetMapping("/slug/{slug}")
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
    fun editProduct(@PathVariable id: Long, @RequestBody dto: EditProductDto): ResponseEntity<ProductDto> {
        val product = productService.edit(id, dto)
        return ResponseEntity<ProductDto>(product.toDto(), HttpStatus.OK)
    }
}