package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.common.util.pagination.PageMapper
import com.dotcom.retail.common.util.pagination.PagedResponse
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(ApiRoutes.Product.BASE)
class ProductController(
    private val productService: ProductService,
    private val productMapper: ProductMapper,
    private val productRepository: ProductRepository
) {

    @GetMapping
    fun getProducts(
        @Validated params: ProductQueryParams,
    ): ResponseEntity<PagedResponse<ProductDto>> {
        val products = productService.query(params)
        return ResponseEntity.ok(products)
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ProductDto> {
        val product = productService.get(id)
        return ResponseEntity.ok(productMapper.toDto(product))
    }

    @PostMapping
    fun create(
        @RequestPart("product") product: CreateProduct,
        @RequestPart("images") imageFiles: List<MultipartFile>?,
    ): ResponseEntity<ProductDto> {
        val product = productService.create(product, imageFiles)
        return ResponseEntity<ProductDto>(productMapper.toDto(product), HttpStatus.CREATED)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun edit(
        @PathVariable id: Long,
        @RequestPart("product") dto: EditProductDto,
        @RequestPart("images") imageFiles: List<MultipartFile>?,
    ): ResponseEntity<ProductDto> {
        val product = productService.edit(id, dto, imageFiles)
        return ResponseEntity<ProductDto>(productMapper.toDto(product), HttpStatus.OK)
    }

    @GetMapping("{productId}${ApiRoutes.Product.IMAGE}/{imageId}")
    fun getImage(@PathVariable productId: Long, @PathVariable imageId: Long): ResponseEntity<Resource> {
        val image = productService.getImage(productId, imageId)
        return ResponseEntity<Resource>.ok().contentType(MediaType.IMAGE_JPEG).body(image)
    }

    @GetMapping(ApiRoutes.Product.SEARCH)
    fun search(query: String): ResponseEntity<PagedResponse<ProductDto>> {
        val pageable = PageRequest.of(0, 10)
        val products = productRepository.searchByText(query, pageable)
        val mapped = PageMapper.toPagedResponse(products.map { productMapper.toDto(it) })
        return ResponseEntity.ok(mapped)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }
}