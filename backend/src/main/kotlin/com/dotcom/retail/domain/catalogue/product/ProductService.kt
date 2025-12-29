package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.exception.ImageNotFoundException
import com.dotcom.retail.common.exception.ProductNotFoundException
import com.dotcom.retail.config.properties.FileProperties
import com.dotcom.retail.domain.catalogue.brand.BrandService
import com.dotcom.retail.domain.catalogue.category.CategoryService
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.domain.catalogue.image.ImageDeletionEvent
import com.dotcom.retail.domain.catalogue.image.ImageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val brandService: BrandService,
    private val categoryService: CategoryService,
    private val imageService: ImageService,
    private val fileProperties: FileProperties,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val productImagePath = Paths.get(fileProperties.image.product.dir)

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
        val brand = dto.brandId?.let(brandService::get)
        val category = dto.categoryId?.let(categoryService::get)
        val images = getImages(dto.images)

        val product = Product(
            name = dto.name,
            sku = dto.sku,
            slug = generateSlug(),
            description = dto.description,
            price = dto.price,
            salePrice = dto.salePrice,
            stock = dto.stock,
            brand = brand,
            category = category,
            attributes = dto.attributes,
            images = images,
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
        handleImages(product.images, getImages(dto.imageIds))

        product.apply {
            name = dto.name
            sku = dto.sku
            slug = generateSlug()
            description = dto.description
            price = dto.price
            salePrice = dto.salePrice
            stock = dto.stock
            brand = dto.brandId?.let(brandService::get)
            category = dto.categoryId?.let(categoryService::get)
            attributes = dto.attributes
            isActive = dto.isActive
        }

        return save(product)
    }

    @Transactional
    fun delete(id: Long) {
        val product = get(id)
        val images = product.images

        if (images.isNotEmpty()) {
            val filePaths = images.map { it.filePath }
            eventPublisher.publishEvent(ImageDeletionEvent(filePaths))
        }

        productRepository.delete(product)
    }

    fun getImage(productId: Long, imageId: Long): Resource {
        val imagePath = imageService.getActiveProductImagePath(productId, imageId)
        return imageService.findFile(imagePath) ?: throw ImageNotFoundException(imageId)
    }

    private fun getImages(imageIds: List<Long>?): MutableList<Image> {
        if (imageIds == null || imageIds.isEmpty()) return mutableListOf()

        val images = imageService.findAllById(imageIds)
        val notFound = imageIds.filterNot { id -> id in images.map { it.id }.toSet() }
        if (notFound.isNotEmpty()) throw ImageNotFoundException(notFound[0])

        return images.toMutableList()
    }

    fun handleImages(currentImages: MutableList<Image>, newImages: MutableList<Image>): MutableList<Image> {
        val newImageIds = newImages.map { it.id }.toSet()
        val orphans = currentImages.filter { it.id !in newImageIds }

        val currentImageIds = currentImages.map { it.id }.toSet()
        val toAdd = newImages.filter { it.id !in currentImageIds }.toMutableList()

//        println("current: $currentImageIds")
//        println("newImages: ${newImageIds}")
//        println("orphans: ${orphans.map { it.id }}")
//        println("toadd: ${toAdd.map { it.id }}")

        currentImages.removeAll(orphans)
        currentImages.addAll(toAdd)

        eventPublisher.publishEvent(ImageDeletionEvent(orphans.map { it.filePath }))
        return toAdd
    }

}