package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.exception.DuplicateImageSortOrderException
import com.dotcom.retail.common.exception.ImageMetadataNotFoundException
import com.dotcom.retail.common.exception.ImageNotFoundException
import com.dotcom.retail.common.exception.ProductNotFoundException
import com.dotcom.retail.config.properties.FileProperties
import com.dotcom.retail.domain.catalogue.brand.BrandService
import com.dotcom.retail.domain.catalogue.category.CategoryService
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.domain.catalogue.image.ImageDeletionEvent
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import com.dotcom.retail.domain.catalogue.image.ImageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
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

    @Transactional
    fun create(dto: CreateProduct, imageFiles: List<MultipartFile>): Product {
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
            images = mutableListOf(),
            isActive = dto.isActive
        )

        val processedImages = mutableListOf<Image>()
        if (dto.images != null && dto.images.isNotEmpty()) {
            try {
                val imageMetaMap = dto.images.associateBy { it.fileName }
                imageFiles.forEach { file ->
                    val meta = imageMetaMap[file.originalFilename]
                        ?: throw ImageMetadataNotFoundException(file.originalFilename)

                    val image = imageService.create(file, meta, productImagePath)
                    processedImages.add(image)
                }
            } catch (e: Exception) {
                if (processedImages.isNotEmpty()) {
                    eventPublisher.publishEvent(ImageDeletionEvent(processedImages.map { it.filePath }))
                }
                throw e
            }
        }

        product.images.addAll(processedImages)

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
    fun edit(id: Long, dto: EditProductDto, imageFiles: List<MultipartFile>): Product {
        val product = get(id)

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

        if (dto.images != null && dto.images.isNotEmpty()) {
            handleImageUpdates(product, dto.images, imageFiles)
        }
        return save(product)
    }

    /**
     * Updates the [Product] images with the provided metadata and files.
     * * This function performs a "diff" operation:
     * 1. Updates existing images based on ID.
     * 2. Uploads and attaches new image files.
     * 3. Removes images that are no longer present in the metadata.
     * @throws ImageNotFoundException if a provided ID does not exist.
     * @throws ImageMetadataNotFoundException if a file is missing for new metadata.
     * @throws DuplicateImageSortOrderException if metadata contains duplicate sortOrders.
     */
    private fun handleImageUpdates(
        product: Product,
        imageMetadata: List<ImageMetadata>,
        imageFiles: List<MultipartFile>
    ) {

        val duplicateSortOrder = imageMetadata.map { it.sortOrder }
            .groupBy { it }
            .filter { it.value.size > 1 }
            .keys

        if (duplicateSortOrder.isNotEmpty()) {
            throw DuplicateImageSortOrderException()
        }

        val currentImages = product.images

        val newExistingImages = mutableListOf<Image>()
        val newImages = mutableListOf<Image>()
        try {
            imageMetadata.forEach { meta ->
                if (meta.id != null) {
                    val existingImage = currentImages.find { it.id == meta.id }
                        ?: throw ImageNotFoundException(meta.id)

                    existingImage.apply {
                        sortOrder = meta.sortOrder
                        altText = meta.altText
                    }

                    newExistingImages.add(existingImage)
                } else {
                    val file = imageFiles.find { it.originalFilename == meta.fileName }
                        ?: throw ImageMetadataNotFoundException(meta.fileName)

                    val image = imageService.create(file, meta, productImagePath)
                    newImages.add(image)
                }
            }
            val toRemove = currentImages.subtract(newExistingImages.toSet())
            currentImages.removeAll(toRemove)
            eventPublisher.publishEvent(ImageDeletionEvent(toRemove.map { it.filePath }))

            currentImages.addAll(newImages)
        } catch (e: Exception) {
            if (newImages.isNotEmpty()) {
                eventPublisher.publishEvent(ImageDeletionEvent(newImages.map { it.filePath }))
            }
            throw e
        }
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