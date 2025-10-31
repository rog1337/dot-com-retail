package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.exception.ProductNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository
) : ProductService {

    override fun find(id: Long): Product? {
        return productRepository.findByIdOrNull(id)
    }

    override fun get(id: Long): Product {
        return productRepository.findByIdOrNull(id) ?: throw ProductNotFoundException(id)
    }

    override fun findBySlug(slug: String): Product? {
        return productRepository.findBySlug(slug)
    }

    override fun getBySlug(slug: String): Product {
        return productRepository.findBySlug(slug) ?: throw ProductNotFoundException(slug)
    }

    override fun create(dto: CreateProductDto): Product {
        val product = dto.toEntity()
        return productRepository.save(product)
        // weightLb
    }

    override fun save(product: Product): Product {
        return productRepository.save(product)
    }

}