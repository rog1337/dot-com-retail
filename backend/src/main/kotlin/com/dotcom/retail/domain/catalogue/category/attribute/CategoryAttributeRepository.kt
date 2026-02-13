package com.dotcom.retail.domain.catalogue.category.attribute

import org.springframework.data.jpa.repository.JpaRepository

interface CategoryAttributeRepository : JpaRepository<CategoryAttribute, Long>{
    fun findByAttribute(attribute: String): List<CategoryAttribute>
}
