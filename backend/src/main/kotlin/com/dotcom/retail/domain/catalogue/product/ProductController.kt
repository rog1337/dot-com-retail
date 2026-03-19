package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.common.util.pagination.PageConstants
import com.dotcom.retail.common.util.pagination.PageMapper
import com.dotcom.retail.common.util.pagination.PagedResponse
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import com.dotcom.retail.domain.catalogue.review.ReviewService
import com.dotcom.retail.domain.catalogue.review.dto.AddReviewRequest
import com.dotcom.retail.domain.catalogue.review.dto.ReviewDto
import jakarta.validation.constraints.Max
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.util.MultiValueMap
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping(ApiRoutes.Product.BASE)
class ProductController(
    private val productService: ProductService,
    private val productMapper: ProductMapper,
    private val productRepository: ProductRepository,
    private val reviewService: ReviewService,
) {

    @GetMapping
    fun getProducts(
        @Validated params: ProductQueryParams,
        @RequestParam attributes: MultiValueMap<String, String>?
    ): ResponseEntity<PagedResponse<ProductDto>> {
        val products = productService.query(params, attributes)
        return ok(products)
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ProductDto> {
        val product = productService.get(id)
        return ok(productMapper.toDto(product))
    }

    @PostMapping
    fun create(
        @RequestPart("product") product: CreateProduct,
        @RequestPart("images") imageFiles: List<MultipartFile>?,
    ): ResponseEntity<ProductDto> {
        val product = productService.create(product, imageFiles)
        return ResponseEntity<ProductDto>(productMapper.toDto(product), HttpStatus.CREATED)
    }

    @PatchMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun update(
        @PathVariable id: Long,
        @RequestPart("product") data: EditProductDto,
        @RequestPart("images") imageFiles: List<MultipartFile>?,
        @RequestPart("image_metadata") imageMetadata: List<ImageMetadata>?
    ): ResponseEntity<ProductDto> {
        val product = productService.update(id, data, imageFiles, imageMetadata)
        return ResponseEntity<ProductDto>(productMapper.toDto(product), HttpStatus.OK)
    }

    @GetMapping(ApiRoutes.Product.SEARCH)
    fun search(query: String): ResponseEntity<PagedResponse<ProductDto>> {
        val pageable = PageRequest.of(0, 10)
        val products = productRepository.searchByText(query, pageable)
        val mapped = PageMapper.toPagedResponse(products.map { productMapper.toDto(it) })
        return ok(mapped)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        productService.delete(id)
        return noContent().build()
    }

    @GetMapping("{productId}" + ApiRoutes.Product.REVIEW)
    fun getReviews(
        @PathVariable productId: Long,
        @RequestParam page: Int = PageConstants.DEFAULT_PAGE,
        @RequestParam @Max(20) pageSize: Int = PageConstants.DEFAULT_PAGE_SIZE,
    ): ResponseEntity<PagedResponse<ReviewDto>> {
        val reviews = reviewService.getReviewsByProductId(productId, page, pageSize)
        return ok(reviews)
    }

    @PostMapping("{productId}" + ApiRoutes.Product.REVIEW)
    fun addReview(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable productId: Long,
        @RequestBody review: AddReviewRequest,
    ): ResponseEntity<Void> {
        reviewService.addReview(userId, productId, review)
        return noContent().build()
    }
}