package com.dotcom.retail.domain.catalogue.review

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReviewVoteRepository : JpaRepository<ReviewVote, Long> {
    fun existsByReviewIdAndUserId(reviewId: Long, userId: UUID): Boolean
    fun findByReviewIdAndUserId(reviewId: Long, userId: UUID): ReviewVote?
}