package com.dotcom.retail.domain.catalogue.review

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.ReviewError
import com.dotcom.retail.common.util.pagination.PageMapper
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import com.dotcom.retail.domain.catalogue.product.ProductReviewsResponse
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.catalogue.product.UserReviewStatusDto
import com.dotcom.retail.domain.catalogue.review.dto.AddReviewRequest
import com.dotcom.retail.domain.catalogue.review.dto.ToggleVoteResponse
import com.dotcom.retail.domain.order.OrderRepository
import com.dotcom.retail.domain.order.OrderStatus
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
    private val reviewMapper: ReviewMapper,
    private val orderRepository: OrderRepository
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
    fun getProductReviews(productId: Long, page: Int, size: Int, userId: UUID?): ProductReviewsResponse {
        val pageable = PageRequest.of(page, size)
        val reviews = reviewRepository.findByProductId(productId, pageable)
        val reviewIds = reviews.content.map { it.id }

        val voteCounts = reviewVoteRepository.findVoteCountsByReviewIds(reviewIds)
            .associate { it.reviewId to it.voteCount }

        val userVotedReviewIds = userId?.let { reviewVoteRepository.findVotedReviewIdsByUser(it, reviewIds) }
            ?: emptySet()

        val status = getUserReviewStatus(userId, productId)
        return ProductReviewsResponse(
            PageMapper.toPagedResponse(reviewMapper.toPagedDto(reviews, voteCounts, userVotedReviewIds)),
            userReviewStatus = status
        )
    }

    fun getUserReviewStatus(userId: UUID?, productId: Long): UserReviewStatusDto {
        if (userId == null) {
            return UserReviewStatusDto(
                hasPurchased = false,
                hasReviewed = false,
                canReview = false
            )
        }

        val hasPurchased = orderRepository.hasUserPurchasedProduct(userId, productId, listOf(OrderStatus.PAID))
        val hasReviewed = reviewRepository.existsByUserIdAndProductId(userId, productId)

        return UserReviewStatusDto(
            hasPurchased = hasPurchased,
            hasReviewed = hasReviewed,
            canReview = hasPurchased && hasReviewed
        )
    }

    fun deleteReview(userId: UUID, reviewId: Long) {
        val review = getReviewById(reviewId)
        if (review.user.id != userId)
            throw AppException(ReviewError.REVIEW_INSUFFICIENT_PRIVILEGES)
        reviewRepository.delete(review)
    }

    fun getReviewById(reviewId: Long): Review {
        return reviewRepository.findByIdOrNull(reviewId)
            ?: throw AppException(ReviewError.REVIEW_NOT_FOUND.withIdentifier(reviewId))
    }

    fun toggleVote(userId: UUID, reviewId: Long): ToggleVoteResponse {
        val review = getReviewById(reviewId)
        if (review.user.id == userId) {
            throw AppException(ReviewError.CANNOT_VOTE_ON_OWN_REVIEW)
        }

        reviewVoteRepository.findByReviewIdAndUserId(reviewId, userId)?.let {
            review.votes.remove(it)
            reviewRepository.save(review)
            reviewVoteRepository.delete(it)
            return ToggleVoteResponse(voted = false)
        }

        val vote = ReviewVote(review = review, user = userService.getById(userId))
        review.votes.add(vote)
        reviewRepository.save(review)
        return ToggleVoteResponse(voted = true)
    }
}