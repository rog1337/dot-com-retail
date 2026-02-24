package com.dotcom.retail.domain.cart

import org.springframework.data.jpa.repository.JpaRepository

interface CartItemRepository : JpaRepository<CartItem, Long> {
}