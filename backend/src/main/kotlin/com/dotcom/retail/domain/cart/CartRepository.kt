package com.dotcom.retail.domain.cart

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CartRepository : JpaRepository<Cart, UUID> {
    fun findByUserId(userId: UUID): Cart?
    fun findBySessionId(sessionId: String): Cart?
}