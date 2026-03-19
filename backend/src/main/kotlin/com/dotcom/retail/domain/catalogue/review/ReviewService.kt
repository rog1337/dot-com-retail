package com.dotcom.retail.domain.catalogue.review

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.ReviewError
import com.dotcom.retail.common.util.pagination.PageMapper
import com.dotcom.retail.common.util.pagination.PagedResponse
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.catalogue.review.dto.AddReviewRequest
import com.dotcom.retail.domain.catalogue.review.dto.ReviewDto
import com.dotcom.retail.domain.user.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.math.roundToLong

@Service
class ReviewService(
    private val userService: UserService,
    private val productService: ProductService,
    private val reviewRepository: ReviewRepository,
    private val productRepository: ProductRepository,
    private val reviewVoteRepository: ReviewVoteRepository,
    private val reviewMapper: ReviewMapper
) {

    @Transactional
    fun addReview(userId: UUID, productId: Long, review: AddReviewRequest): Review {
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw AppException(ReviewError.REVIEW_ALREADY_EXISTS)
        }

        val user = userService.getById(userId)
        val product = productService.get(productId)

        val review = Review(
            product = product,
            user = user,
            rating = review.rating,
            body = review.body,
        )
        reviewRepository.save(review)

        val newCount = product.reviewCount + 1
        val newTotalScore = (product.averageRating * product.reviewCount) + review.rating

        product.apply {
            reviewCount = newCount
            averageRating = (newTotalScore / newCount).roundToLong() * 10 / 10.0
        }
        productRepository.save(product)
        return review
    }

    @Transactional
    fun getReviewsByProductId(productId: Long, page: Int, pageSize: Int): PagedResponse<ReviewDto> {
        val pageable = PageRequest.of(page, pageSize)
        val reviews = reviewRepository.findByProductId(productId, pageable)
        return PageMapper.toPagedResponse(reviewMapper.toPagedDto(reviews))
    }

    fun deleteReview(userId: UUID, reviewId: Long) {
        val review = getReviewById(reviewId)
        if (review.user.id != userId)
            throw AppException(ReviewError.REVIEW_INSUFFICIENT_PRIVILEGES)
        reviewRepository.delete(review)
    }


    fun voteReview(userId: UUID, reviewId: Long) {
        if (reviewVoteRepository.existsByReviewIdAndUserId(reviewId, userId))
            return

        val review = getReviewById(reviewId)
        val vote = ReviewVote(review = review, user = userService.getById(userId))
        review.votes.add(vote)
        reviewRepository.save(review)
    }

    fun unvoteReview(userId: UUID, reviewId: Long) {
        val review = getReviewById(reviewId)

        val vote = reviewVoteRepository.findByReviewIdAndUserId(reviewId, userId)
            ?: throw AppException(ReviewError.REVIEW_VOTE_NOT_FOUND)

        review.votes.remove(vote)
        reviewRepository.save(review)
        reviewVoteRepository.delete(vote)
    }

    fun getReviewById(reviewId: Long): Review {
        return reviewRepository.findByIdOrNull(reviewId)
            ?: throw AppException(ReviewError.REVIEW_NOT_FOUND.withIdentifier(reviewId))
    }
}