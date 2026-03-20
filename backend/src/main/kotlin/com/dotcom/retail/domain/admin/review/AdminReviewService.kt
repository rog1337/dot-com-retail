package com.dotcom.retail.domain.admin.review

import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.domain.catalogue.review.ReviewRepository
import com.dotcom.retail.domain.catalogue.review.ReviewService
import org.springframework.stereotype.Service

@Service
class AdminReviewService(
    private val reviewService: ReviewService,
    private val reviewRepository: ReviewRepository,
    private val emailService: EmailService
) {

    fun deleteReview(reviewId: Long, cause: String?) {
        val review = reviewService.getReviewById(reviewId)
        reviewRepository.delete(review)
        emailService.sendReviewRemoved(review.user.email, reviewId, review.body, cause)
    }
}