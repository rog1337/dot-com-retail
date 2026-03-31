package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.CategoryAttributeError
import com.dotcom.retail.common.exception.ImageError
import com.dotcom.retail.common.exception.ImageMetadataError
import com.dotcom.retail.common.exception.ProductError
import com.dotcom.retail.common.util.pagination.PageMapper
import com.dotcom.retail.common.util.pagination.PagedResponse
import com.dotcom.retail.config.properties.FileProperties
import com.dotcom.retail.domain.admin.product.dto.CreateProduct
import com.dotcom.retail.domain.admin.product.dto.EditProductDto
import com.dotcom.retail.domain.catalogue.brand.BrandService
import com.dotcom.retail.domain.catalogue.category.CategoryService
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeMetadataService
import com.dotcom.retail.domain.catalogue.filter.ValueCount
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.domain.catalogue.image.ImageDeletionEvent
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import com.dotcom.retail.domain.catalogue.image.ImageService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MultiValueMap
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
    private val attributeMetadataService: AttributeMetadataService,
) {

    companion object {
        const val QUERY_PARAM_ATTRIBUTE_PREFIX = "attr_"
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun find(id: Long): Product? {
        return productRepository.findByIdOrNull(id)
    }


    fun get(id: Long): Product {
        return productRepository.findById(id)
            .orElseThrow { AppException(ProductError.PRODUCT_NOT_FOUND.withIdentifier(id)) }
    }

    fun findAll(specification: Specification<Product>, pageable: Pageable): Page<Product> {
        return productRepository.findAll(specification, pageable)
    }

    fun findAllById(ids: Set<Long>): Set<Product> {
        if (ids.isEmpty()) return emptySet()
        return productRepository.findAllById(ids).toSet()
    }

    fun getAllById(ids: Set<Long>): Set<Product> {
        if (ids.isEmpty()) throw AppException(ProductError.PRODUCT_IDS_NOT_PROVIDED)
        val products = productRepository.findAllById(ids).toSet()
        if (products.size != ids.size) {
            throw AppException(ProductError.PRODUCT_NOT_FOUND.withIdentifier(ids.subtract(products.map { it.id }.toSet())))
        }

        return products
    }

    fun query(params: ProductQueryParams, attributes: MultiValueMap<String, String>?): PagedResponse<ProductDto> {
        val query = productMapper.queryParamsToQuery(params, parseAttributeParams(attributes))
        val spec = productSpecifications.fromParams(query)
        val pageable = PageRequest.of(params.page, params.size)
        val productPage = findAll(spec, pageable)

        return PageMapper.toPagedResponse(productPage.map { product -> productMapper.toDto(product)})
    }

    private fun parseAttributeParams(attributes: MultiValueMap<String, String>?): List<ProductAttributeDto> {
        if (attributes.isNullOrEmpty()) return emptyList()
        return attributes.entries
            .filter { (key,_) -> key.startsWith(QUERY_PARAM_ATTRIBUTE_PREFIX) }
            .map { (key, values) ->
                val attributeName = key.removePrefix(QUERY_PARAM_ATTRIBUTE_PREFIX)

                val normalizedValues = values
                    .flatMap { it.split(",") }
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                ProductAttributeDto(attributeName, normalizedValues)
            }
            .filter { it.values.isNotEmpty() }
    }


    @Transactional
    fun create(dto: CreateProduct, imageFiles: List<MultipartFile>?): Product {
        val product = Product(
            name = dto.name,
            sku = dto.sku,
            description = dto.description,
            price = dto.price,
            salePrice = dto.salePrice,
            stock = dto.stock,
            brand = dto.brandId?.let(brandService::get),
            category = dto.categoryId?.let(categoryService::get),
            attributes = handleAttributes(dto.attributes),
            images = mutableListOf(),
            isActive = dto.isActive
        )

        val processedImages = mutableListOf<Image>()
        if (!dto.images.isNullOrEmpty() && !imageFiles.isNullOrEmpty()) {
            try {
                val imageMetaMap = dto.images.associateBy { it.fileName }
                imageFiles.forEach { file ->
                    val meta = imageMetaMap[file.originalFilename]
                        ?: throw AppException(ImageMetadataError.IMAGE_METADATA_NOT_PROVIDED.withIdentifier(file.originalFilename.toString()))

                    val image = imageService.create(file, meta, fileProperties.productPath)
                    processedImages.add(image)
                }
            } catch (e: Exception) {
                if (processedImages.isNotEmpty()) {
                    eventPublisher.publishEvent(ImageDeletionEvent(
                        processedImages.map { fileProperties.productPathFull.resolve(it.fileName) })
                    )
                }
                throw e
            }
        }

        product.images.addAll(processedImages)
        val saved = productRepository.save(product)
        logger.debug("Saved product: {}", saved)
        return saved
    }

    private fun handleAttributes(attributes: List<ProductAttributeDto>?): MutableMap<String, MutableList<Any>>? {
        return attributes
            ?.groupBy { it.name }
            ?.mapValues { (_, list) ->
                list.flatMap { attr ->
                    val attribute = attributeMetadataService.getAttribute(attr.name)
                    if (attribute?.dataType == AttributeDataType.NUMBER) {
                        attr.values.map {
                            val double = it.toString().toDoubleOrNull()
                                ?: throw AppException(CategoryAttributeError.CATEGORY_ATTRIBUTE_INCOMPATIBLE_TYPE
                                    .withIdentifier(it))
                            if (double % 1.0 == 0.0) double.toInt() else double
                        }
                    } else {
                        attr.values
                    }
                }.toMutableList()
            }?.toMutableMap()
    }

    fun save(product: Product): Product {
        return productRepository.save(product)
    }

    @Transactional
    fun update(id: Long, data: EditProductDto, imageFiles: List<MultipartFile>?, imageMetadata: List<ImageMetadata>?): Product {
        val product = get(id)

        if (data.name.isPresent) product.name = data.name.get()
        if (data.sku.isPresent) product.sku = data.sku.get()
        if (data.description.isPresent) product.description = data.description.get()
        if (data.price.isPresent) product.price = data.price.get()
        if (data.salePrice.isPresent) product.salePrice = data.salePrice.get()
        if (data.stock.isPresent) product.stock = data.stock.get()
        if (data.isActive.isPresent) product.isActive = data.isActive.get()

        if (data.brandId.isPresent) {
            val brandId = data.brandId.get()
            if (brandId == null) product.brand = null
            else product.brand = brandService.get(brandId)
        }

        if (data.categoryId.isPresent) {
            val categoryId = data.categoryId.get()
            if (categoryId == null) product.category = null
            else product.category = categoryService.get(categoryId)
        }

        if (data.attributes.isPresent) {
            val attributes = data.attributes.get()
            product.attributes = handleAttributes(attributes) ?: mutableMapOf()
        }

        if (data.images.isPresent) {
            val updatableImages = data.images.get()
            if (updatableImages.isNullOrEmpty()) {
                val images = product.images
                eventPublisher.publishEvent(ImageDeletionEvent(images.map { fileProperties.productPathFull.resolve(it.fileName) }))
                product.images.clear()
            }
            else {
                val sortOrders = updatableImages.map { it.sortOrder }
                checkDuplicateImageSortOrder(sortOrders)

                val images = imageService.getAllById(updatableImages.map { it.id }.toSet()).toSet()
                val toRemove = product.images.subtract(images)
                eventPublisher.publishEvent(ImageDeletionEvent(toRemove.map { fileProperties.productPathFull.resolve(it.fileName) }))
                product.images.removeAll(toRemove)

                images.forEach { image ->
                    image.sortOrder = updatableImages.find { it.id == image.id }?.sortOrder ?: 0
                    image.altText = updatableImages.find { it.id == image.id }?.altText ?: ""
                }
            }
        }

        if (!imageFiles.isNullOrEmpty() && !imageMetadata.isNullOrEmpty()) {
            handleNewImages(product, imageMetadata, imageFiles)
        }

        return save(product)
    }

    private fun checkDuplicateImageSortOrder(values: List<Int>) {
        val duplicateSortOrder = values.groupBy { it }
            .filter { it.value.size > 1 }
            .keys

        if (duplicateSortOrder.isNotEmpty())
            throw AppException(ImageMetadataError.IMAGE_METADATA_DUPLICATE_SORT_ORDER
                .withIdentifier(duplicateSortOrder.joinToString(",")))
    }

    private fun handleNewImages(
        product: Product,
        imageMetadata: List<ImageMetadata>,
        imageFiles: List<MultipartFile>
    ) {
        val sortOrders = imageMetadata.map { it.sortOrder }
        checkDuplicateImageSortOrder(sortOrders)

        val images = mutableListOf<Image>()
        try {
            imageMetadata.forEach { meta ->
                val file = imageFiles.find { it.originalFilename == meta.fileName }
                    ?: throw AppException(ImageError.IMAGE_NOT_PROVIDED.withIdentifier(meta.fileName))

                val image = imageService.create(file, meta, fileProperties.productPath)
                images.add(image)
            }

        } catch (e: Exception) {
            println(images.isNotEmpty())
            if (images.isNotEmpty()) {
                eventPublisher.publishEvent(ImageDeletionEvent(images.map { fileProperties.productPathFull.resolve(it.fileName) }))
            }
            throw e
        }

        product.images.addAll(images)
    }

    @Transactional
    fun delete(id: Long) {
        val product = get(id)
        val images = product.images

        if (images.isNotEmpty()) {
            val filePaths = images.map { fileProperties.productPathFull.resolve(it.fileName) }
            eventPublisher.publishEvent(ImageDeletionEvent(filePaths))
        }

        productRepository.delete(product)
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
