package com.dotcom.retail.domain.catalogue.category

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface CategoryRepository : JpaRepository<Category, Long> {
}