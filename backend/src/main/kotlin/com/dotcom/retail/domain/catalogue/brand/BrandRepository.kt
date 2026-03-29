package com.dotcom.retail.domain.catalogue.brand

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface BrandRepository : JpaRepository<Brand, Long> {
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Brand>
}