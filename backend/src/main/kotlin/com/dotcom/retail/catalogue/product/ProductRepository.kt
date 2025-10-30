package com.dotcom.retail.catalogue.product

import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySlug(slug: String): Product?
}
