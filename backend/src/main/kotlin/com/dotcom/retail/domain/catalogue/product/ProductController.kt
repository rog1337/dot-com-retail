package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.constants.ApiRoutes.Product
import com.dotcom.retail.common.util.pagination.PageConstants
import com.dotcom.retail.common.util.pagination.PagedResponse
import com.dotcom.retail.domain.catalogue.review.ReviewService
import com.dotcom.retail.domain.catalogue.review.dto.AddReviewRequest
import jakarta.validation.constraints.Max
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.status
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.util.MultiValueMap
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(Product.BASE)
class ProductController(
    private val productService: ProductService,
    private val productMapper: ProductMapper,
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

    @GetMapping("{productId}" + Product.REVIEW)
    fun getReviews(
        @AuthenticationPrincipal userId: UUID?,
        @PathVariable productId: Long,
        @RequestParam page: Int = PageConstants.DEFAULT_PAGE,
        @RequestParam @Max(20) size: Int = PageConstants.DEFAULT_PAGE_SIZE,
    ): ResponseEntity<ProductReviewsResponse> {
        val response = reviewService.getProductReviews(productId, page, size, userId)
        return ok(response)
    }

    @PostMapping("{productId}" + Product.REVIEW)
    fun addReview(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable productId: Long,
        @RequestBody review: AddReviewRequest,
    ): ResponseEntity<Void> {
        reviewService.addReview(userId, productId, review)
        return status(HttpStatus.CREATED).build()
    }
}