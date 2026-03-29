package com.dotcom.retail.domain.catalogue.review

import com.dotcom.retail.domain.admin.review.dto.AdminReviewDto
import com.dotcom.retail.domain.catalogue.review.dto.ReviewAuthorDto
import com.dotcom.retail.domain.catalogue.review.dto.ReviewDto
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserMapper
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class ReviewMapper(private val userMapper: UserMapper) {

    fun toPagedDto(reviews: Page<Review>): Page<ReviewDto> {
        return reviews.map { toDto(it) }
    }

    fun toDto(review: Review): ReviewDto = ReviewDto(
        id = review.id,
        rating = review.rating,
        body = review.body,
        votes = review.votes.size,
        author = toAuthorDto(review.user)
    )

    fun toAuthorDto(user: User): ReviewAuthorDto = ReviewAuthorDto(
        id = user.id,
        displayName = user.displayName
    )

    fun toAdminDto(review: Review): AdminReviewDto = AdminReviewDto(
        id = review.id,
        rating = review.rating,
        body = review.body,
        votes = review.votes.size,
        author = userMapper.toAdminDto(review.user)
    )
}