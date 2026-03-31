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

    fun toPagedDto(
        reviews: Page<Review>,
        voteCounts: Map<Long, Long>,
        userVotedReviewIds: Collection<Long>
    ): Page<ReviewDto> {
        return reviews.map {
            toDto(
                review = it,
                votes = voteCounts[it.id] ?: 0,
                hasVoted = it.id in userVotedReviewIds
            )
        }
    }

    fun toDto(review: Review, votes: Long, hasVoted: Boolean): ReviewDto = ReviewDto(
        id = review.id,
        rating = review.rating,
        body = review.body,
        votes = votes,
        author = toAuthorDto(review.user),
        hasVoted = hasVoted,
        createdAt = review.createdAt,
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