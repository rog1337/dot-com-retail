package com.dotcom.retail.domain.catalogue.review

import com.dotcom.retail.domain.catalogue.review.dto.ReviewVoteCount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ReviewVoteRepository : JpaRepository<ReviewVote, Long> {
    fun findByReviewIdAndUserId(reviewId: Long, userId: UUID): ReviewVote?

    @Query("""
        SELECT new 
        com.dotcom.retail.domain.catalogue.review.dto.ReviewVoteCount(v.review.id, COUNT(v))
        FROM ReviewVote v
        WHERE v.review.id IN :reviewIds
        GROUP BY v.review.id
    """)
    fun findVoteCountsByReviewIds(reviewIds: List<Long>): List<ReviewVoteCount>

    @Query("""
        SELECT v.review.id
        FROM ReviewVote v 
        WHERE v.user.id = :userId 
        AND v.review.id IN :reviewIds
    """)
    fun findVotedReviewIdsByUser(userId: UUID, reviewIds: List<Long>): Set<Long>
}