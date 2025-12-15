package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.exception.ProductNotFoundException
import com.dotcom.retail.domain.catalogue.brand.BrandService
import com.dotcom.retail.domain.catalogue.category.CategoryService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val brandService: BrandService,
    private val categoryService: CategoryService
) {

    fun find(id: Long): Product? {
        return productRepository.findByIdOrNull(id)
    }

    fun get(id: Long): Product {
        return productRepository.findByIdOrNull(id) ?: throw ProductNotFoundException(id)
    }

    fun findBySlug(slug: String): Product? {
        return productRepository.findBySlug(slug)
    }

    fun getBySlug(slug: String): Product {
        return productRepository.findBySlug(slug) ?: throw ProductNotFoundException(slug)
    }

    fun create(dto: CreateProductDto): Product {
        val product = Product(
            name = dto.name,
            sku = dto.sku,
            slug = generateSlug(),
            description = dto.description,
            price = dto.price,
            salePrice = dto.salePrice,
            stock = dto.stock,
            brand = dto.brandId?.let(brandService::get),
            category = dto.categoryId?.let(categoryService::get),
            attributes = dto.attributes,
//            images = dto.images, TODO
            isActive = dto.isActive
        )

        return productRepository.save(product)
    }

    //TODO
    fun generateSlug(): String {
        return ""
    }

    fun save(product: Product): Product {
        return productRepository.save(product)
    }

    @Transactional
    fun edit(id: Long, dto: EditProductDto): Product {
        val product = get(id)

        product.name = dto.name
        product.sku = dto.sku
        product.slug = generateSlug()
        product.description = dto.description
        product.price = dto.price
        product.salePrice = dto.salePrice
        product.stock = dto.stock
        product.brand = dto.brandId?.let(brandService::get)
        product.category = dto.categoryId?.let(categoryService::get)
        product.attributes = dto.attributes
        product.isActive = dto.isActive

        return save(product)
    }

}