package com.dotcom.retail.domain.catalogue.review

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ReviewRepository : JpaRepository<Review, Long> {
    fun existsByUserIdAndProductId(userId: UUID, productId: Long): Boolean

    @Query("""
        SELECT r FROM Review r
        WHERE r.product.id = :productId
        ORDER BY SIZE(r.votes) DESC
    """)
    @EntityGraph(attributePaths = ["user"])
    fun findByProductId(productId: Long, pageable: Pageable): Page<Review>
}