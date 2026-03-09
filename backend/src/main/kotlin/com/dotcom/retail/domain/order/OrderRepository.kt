package com.dotcom.retail.domain.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByUserId(userId: UUID): Set<Order>
    fun findBySessionId(sessionId: String): Set<Order>
    fun findByIntentId(intentId: String): Order?

    @EntityGraph(attributePaths = ["items"])
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Order>
}