package com.dotcom.retail.domain.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

interface OrderRepository : JpaRepository<Order, UUID> {

    @Query("""
        SELECT COUNT(o) > 0 FROM Order o
        JOIN o.items i
        WHERE o.user.id = :userId
        AND i.product.id = :productId
        AND o.status IN :statuses
    """)
    fun hasUserPurchasedProduct(
        @Param("userId") userId: UUID,
        @Param("productId") productId: Long,
        @Param("statuses") statuses: Collection<OrderStatus>
    ): Boolean

    @EntityGraph(attributePaths = ["items"])
    fun findByUserId(userId: UUID): Set<Order>

    @EntityGraph(attributePaths = ["items"])
    fun findBySessionId(sessionId: String): Set<Order>

    @EntityGraph(attributePaths = ["items"])
    fun findByIntentId(intentId: String): Order?

    @EntityGraph(attributePaths = ["items"])
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Order>

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :cutoff")
    fun findAbandonedOrders(cutoff: Instant, status: OrderStatus = OrderStatus.PENDING_PAYMENT): List<Order>

    @Query("""
        SELECT o FROM Order o
        WHERE o.user.id = :userId
        AND (:status IS NULL OR o.status = :status)
    """)
    @EntityGraph(attributePaths = ["items", "items.product"])
    fun findByUserIdAndStatus(userId: UUID, status: OrderStatus?, pageable: Pageable): Page<Order>

    @Query("""
        SELECT o FROM Order o 
        WHERE o.sessionId = :sessionId 
        AND (:status IS NULL OR o.status = :status)
    """)
    @EntityGraph(attributePaths = ["items", "items.product"])
    fun findBySessionIdAndStatus(sessionId: String, status: OrderStatus?, pageable: Pageable): Page<Order>
}