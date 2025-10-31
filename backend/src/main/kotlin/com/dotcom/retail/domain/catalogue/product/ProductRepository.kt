package com.dotcom.retail.domain.catalogue.product

import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySlug(slug: String): Product?
}
