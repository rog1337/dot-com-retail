package com.dotcom.retail.domain.order

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByUserId(userId: UUID): Set<Order>
    fun findBySessionId(sessionId: String): Set<Order>
    fun findByIntentId(intentId: String): Order?
}