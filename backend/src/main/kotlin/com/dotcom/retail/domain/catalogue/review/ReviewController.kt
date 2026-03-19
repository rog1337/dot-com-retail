package com.dotcom.retail.domain.catalogue.review

import com.dotcom.retail.common.constants.ApiRoutes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.status
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(ApiRoutes.Review.BASE)
class ReviewController(private val reviewService: ReviewService) {

    @DeleteMapping("{reviewId}")
    fun deleteReview(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable(required = true) reviewId: Long,
    ): ResponseEntity<Void> {
        reviewService.deleteReview(userId, reviewId)
        return noContent().build()
    }

    @PostMapping("{reviewId}" + ApiRoutes.Review.VOTE)
    fun voteReview(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable(required = true) reviewId: Long,
    ): ResponseEntity<Void> {
        reviewService.voteReview(userId, reviewId)
        return status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("{reviewId}" + ApiRoutes.Review.VOTE)
    fun unvoteReview(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable(required = true) reviewId: Long,
    ): ResponseEntity<Void> {
        reviewService.unvoteReview(userId, reviewId)
        return noContent().build()
    }
}