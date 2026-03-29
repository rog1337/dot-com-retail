package com.dotcom.retail.domain.catalogue.category

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun existsByName(name: String): Boolean

    @EntityGraph(attributePaths = ["attributes"])
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Category>
}