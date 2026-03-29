package com.dotcom.retail.domain.catalogue.category.attribute

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CategoryAttributeRepository : JpaRepository<CategoryAttribute, Long>{
    fun findByAttribute(attribute: String): List<CategoryAttribute>

    @Query("""
        SELECT ca
        FROM CategoryAttribute ca
        WHERE LOWER(ca.attribute) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(ca.label) LIKE LOWER(CONCAT('%', :query, '%')) 
    """)
    @EntityGraph(attributePaths = ["categories"])
    fun searchByAttributeOrLabel(query: String, pageable: Pageable): Page<CategoryAttribute>
}
