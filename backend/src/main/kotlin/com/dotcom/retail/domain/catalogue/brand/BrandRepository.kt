package com.dotcom.retail.domain.catalogue.brand

import org.springframework.data.jpa.repository.JpaRepository

interface BrandRepository : JpaRepository<Brand, Long> {
}