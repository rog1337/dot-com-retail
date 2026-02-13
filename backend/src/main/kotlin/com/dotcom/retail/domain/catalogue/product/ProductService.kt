package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.exception.BadRequestException
import com.dotcom.retail.common.exception.NotFoundException
import com.dotcom.retail.common.util.pagination.PageMapper
import com.dotcom.retail.common.util.pagination.PagedResponse
import com.dotcom.retail.config.properties.FileProperties
import com.dotcom.retail.domain.catalogue.brand.BrandService
import com.dotcom.retail.domain.catalogue.category.CategoryService
import com.dotcom.retail.domain.catalogue.filter.ValueCount
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.domain.catalogue.image.ImageDeletionEvent
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import com.dotcom.retail.domain.catalogue.image.ImageService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val brandService: BrandService,
    private val categoryService: CategoryService,
    private val imageService: ImageService,
    private val fileProperties: FileProperties,
    private val eventPublisher: ApplicationEventPublisher,
    private val productMapper: ProductMapper,
    private val productSpecifications: ProductSpecifications,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun find(id: Long): Product? {
        return productRepository.findByIdOrNull(id)
    }

    fun get(id: Long): Product {
        return productRepository.findByIdOrNull(id) ?: throw NotFoundException(Product::class.simpleName, id)
    }

    fun findBySlug(slug: String): Product? {
        return productRepository.findBySlug(slug)
    }

    fun getBySlug(slug: String): Product {
        return productRepository.findBySlug(slug) ?: throw NotFoundException(Product::class.simpleName, slug)
    }

    fun findAll(specification: Specification<Product>, pageable: Pageable): Page<Product> {
        return productRepository.findAll(specification, pageable)
    }

    fun query(params: ProductQueryParams): PagedResponse<ProductDto> {
        val spec = productSpecifications.fromParams(params)
        val pageable = PageRequest.of(params.page, params.pageSize)
        val productPage = findAll(spec, pageable)

        return PageMapper.toPagedResponse(productPage.map { product -> productMapper.toDto(product)})
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
            // attributes = dto.attributes?.groupBy { it.name }?.mapValues { (_, list) -> list.flatMap { it.values } } as MutableMap<String, MutableList<Any>>,
            images = mutableListOf(),
            isActive = dto.isActive
        )

        val processedImages = mutableListOf<Image>()
        if (!dto.images.isNullOrEmpty()) {
            try {
                val imageMetaMap = dto.images.associateBy { it.fileName }
                imageFiles.forEach { file ->
                    val meta = imageMetaMap[file.originalFilename]
                        ?: throw NotFoundException(ImageMetadata::class.simpleName, file.originalFilename)

                    val image = imageService.create(file, meta, fileProperties.productPath)
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
        val saved = productRepository.save(product)
        logger.debug("Saved product: {}", saved)
        return saved
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
//            attributes = dto.attributes?.groupBy { it.name }?.mapValues { (_, list) -> list.flatMap { it.values }} as MutableMap<String, MutableList<Any>>
            isActive = dto.isActive
        }

        if (!dto.images.isNullOrEmpty()) {
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
     * @throws NotFoundException if a provided ID does not exist.
     * @throws NotFoundException if a file is missing for new metadata.
     * @throws BadRequestException if metadata contains duplicate sortOrders.
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
            throw BadRequestException("Duplicate image sort order")
        }

        val currentImages = product.images

        val newExistingImages = mutableListOf<Image>()
        val newImages = mutableListOf<Image>()
        try {
            imageMetadata.forEach { meta ->
                if (meta.id != null) {
                    val existingImage = currentImages.find { it.id == meta.id }
                        ?: throw NotFoundException(Image::class.simpleName, meta.id)

                    existingImage.apply {
                        sortOrder = meta.sortOrder
                        altText = meta.altText
                    }

                    newExistingImages.add(existingImage)
                } else {
                    val file = imageFiles.find { it.originalFilename == meta.fileName }
                        ?: throw NotFoundException(ImageMetadata::class.simpleName, meta.fileName)

                    val image = imageService.create(file, meta, fileProperties.productPath)
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
        return imageService.findFile(imagePath) ?: throw NotFoundException(Image::class.simpleName, imageId)
    }

    fun getBrandCounts(categoryId: Long): List<ProductBrandCount> {
        return productRepository.getBrandCounts(categoryId)
    }

    fun findAttributeCounts(categoryId: Long, attribute: String): List<ValueCount> {
        return productRepository.findAttributeCounts(categoryId, attribute)
    }

    fun findPriceRange(categoryId: Long): PriceRange {
        return productRepository.findPriceRange(categoryId)
    }
}
