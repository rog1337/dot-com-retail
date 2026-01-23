package com.dotcom.retail.domain.catalogue.category

import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun existsByName(name: String): Boolean
}