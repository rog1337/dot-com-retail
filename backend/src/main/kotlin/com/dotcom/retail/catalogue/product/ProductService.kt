package com.dotcom.retail.catalogue.product

import org.springframework.http.ResponseEntity
import java.util.Optional

interface ProductService {
    fun find(id: Long): Product?
    fun get(id: Long): Product

    fun findBySlug(slug: String): Product?
    fun getBySlug(slug: String): Product

    fun create(dto: CreateProductDto): Product

    fun save(product: Product): Product

}