package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.exception.ProductNotFoundException
import com.dotcom.retail.domain.catalogue.product.dto.CreateProductDto
import com.dotcom.retail.domain.catalogue.product.dto.ProductDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    @Transactional
    override fun edit(dto: ProductDto): Product {
        val product = get(dto.id)

//        val category =

        product.name = dto.name
        // todo product.slug
        product.productDescription = dto.productDescription
        product.storeDescription = dto.storeDescription
        product.price = dto.price
        product.stock = dto.stock
        product.brand = dto.brand
        product.category = dto.category
//        product.images = dto.images
//        product.attributes =

    }

}