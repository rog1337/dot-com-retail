package com.dotcom.retail.domain.admin.review

import com.dotcom.retail.common.constants.ApiRoutes.Admin
import com.dotcom.retail.domain.admin.review.dto.AdminReviewDto
import com.dotcom.retail.domain.admin.review.dto.DeleteReviewRequest
import com.dotcom.retail.domain.catalogue.review.ReviewMapper
import com.dotcom.retail.domain.catalogue.review.ReviewService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Admin.Review.BASE)
class AdminReviewController(
    private val adminReviewService: AdminReviewService,
    private val reviewService: ReviewService,
    private val reviewMapper: ReviewMapper
) {
    @GetMapping("{reviewId}")
    fun getReview(@PathVariable reviewId: Long): ResponseEntity<AdminReviewDto> {
        val review = reviewService.getReviewById(reviewId)
        return ok(reviewMapper.toAdminDto(review))
    }

    @DeleteMapping("{reviewId}")
    fun deleteReview(
        @PathVariable(required = true) reviewId: Long,
        @RequestBody request: DeleteReviewRequest?
    ): ResponseEntity<Void> {
        adminReviewService.deleteReview(reviewId, request?.cause)
        return noContent().build()
    }
}