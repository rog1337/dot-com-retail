package com.dotcom.retail.config.db

import com.dotcom.retail.config.properties.FileProperties
import com.dotcom.retail.domain.catalogue.brand.Brand
import com.dotcom.retail.domain.catalogue.brand.BrandRepository
import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.category.CategoryRepository
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttribute
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeRepository
import com.dotcom.retail.domain.catalogue.category.attribute.FilterType
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.domain.catalogue.image.ImageRepository
import com.dotcom.retail.domain.catalogue.image.ImageService
import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.nio.file.Files
import java.util.UUID
import javax.sql.DataSource

@Component
class DatabaseInitializer(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val fileProperties: FileProperties,
    private val imageRepository: ImageRepository,
    private val categoryAttributeRepository: CategoryAttributeRepository,
    private val categoryRepository: CategoryRepository,
    private val dataSource: DataSource,
    private val imageService: ImageService
) : CommandLineRunner {

    private val logger = org.slf4j.LoggerFactory.getLogger(this.javaClass)

    @Transactional
    override fun run(vararg args: String?) {
        insertIndexes()
        generateData()
    }

    fun insertIndexes() {
        val resource = ClassPathResource("db/migration/indexes.sql")
        val databasePopulator = ResourceDatabasePopulator(resource)
        try {
            databasePopulator.execute(dataSource)
        } catch (e: Exception) {
            logger.debug("Failed to execute indexes: ${e.message}")
        }
    }

    fun generateData() {
        if (productRepository.count() > 0) return

        val brandMap = brands()
        val tyreCategory = categories()
        products(brandMap, tyreCategory)
    }

    fun categoryAttributes(): List<CategoryAttribute> {
        val attributes = listOf(
            CategoryAttribute(attribute = "diameter", label = "Diameter", dataType = AttributeDataType.NUMBER, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0),
            CategoryAttribute(attribute = "type", label = "Type", dataType = AttributeDataType.TEXT, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0),
            CategoryAttribute(attribute = "width", label = "Width", dataType = AttributeDataType.NUMBER, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0),
            CategoryAttribute(attribute = "height", label = "Height", dataType = AttributeDataType.NUMBER, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0),
        )

        return categoryAttributeRepository.saveAll(attributes)
    }

    fun categories(): Category {
        val category = Category(
            name = "Tyres",
            attributes = categoryAttributes().toMutableList(),
        )

        return categoryRepository.save(category)
    }

    fun images(name: String): Image {
        val imageData = mapOf(
            "Michelin" to "sample_michelin.jpg",
            "Continental" to "sample_continental.jpg",
            "Goodyear" to "sample_goodyear.png",
            "Pirelli" to "sample_pirelli.jpg",
            "Bridgestone" to "sample_bridgestone.jpeg",
        )

        val fileName = UUID.randomUUID().toString() + imageData[name]

        val image = Image(
                fileName = fileName,
                contentType = MediaType.IMAGE_JPEG_VALUE,
                sortOrder = 0,
                altText = name
            )

        val path = fileProperties.productPathFull
        val sourceDir = path.resolve("sample/${imageData[name]}")
        val targetDir = path.resolve(fileName)

        if (!Files.exists(targetDir)) {
            Files.copy(sourceDir, targetDir)
        }

        return imageRepository.save(image)
    }

    fun products(brandMap: Map<String, Brand>, category: Category): List<Product> {
        val tyres = listOf(
            TyreData("Michelin", "Primacy 4", 205, 55, 16, "Summer", 120.0),
            TyreData("Continental", "WinterContact TS 870", 205, 55, 16, "Winter", 115.5),
            TyreData("Bridgestone", "Turanza T005", 225, 45, 17, "Summer", 135.0),
            TyreData("Goodyear", "Vector 4Seasons", 225, 45, 17, "All-Season", 140.0),
            TyreData("Michelin", "Pilot Sport 4S", 245, 40, 19, "Summer", 280.0),
            TyreData("Pirelli", "P Zero", 255, 35, 19, "Summer", 265.0),
            TyreData("Continental", "SportContact 7", 235, 40, 18, "Summer", 195.0),
            TyreData("Bridgestone", "Blizzak DM-V3", 265, 60, 18, "Winter", 210.0),
            TyreData("Michelin", "Latitude Sport 3", 255, 50, 19, "Summer", 230.0),
            TyreData("Goodyear", "Eagle F1 Asymmetric", 275, 45, 20, "Summer", 310.0),
            TyreData("Pirelli", "Cinturato P7", 195, 65, 15, "Summer", 95.0),
            TyreData("Continental", "AllSeasonContact", 195, 65, 15, "All-Season", 100.0)
        )

        val products = tyres.map { data ->
            val brand = brandMap[data.brand] ?: throw RuntimeException("Brand ${data.brand} missing")
            val fullName = "${data.brand} ${data.model} ${data.width}/${data.height} R${data.diameter}"

            val images = mutableListOf(images(brand.name))

            Product(
                name = fullName,
                sku = "TYRE-${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
                description = "Premium ${data.type} tyre from ${data.brand}.",
                price = BigDecimal.valueOf(data.price),
                salePrice = BigDecimal.valueOf(data.price * 0.9),
                stock = (10..100).random(),
                isActive = true,
                brand = brand,
                attributes = mutableMapOf(
                    "width" to mutableListOf(data.width),
                    "height" to mutableListOf(data.height),
                    "diameter" to mutableListOf(data.diameter),
                    "type" to mutableListOf(data.type)
                ),
                images = images,
                category = category,
            )
        }
        return productRepository.saveAll(products)
    }

    fun brands(): Map<String, Brand> {
        if (brandRepository.count() > 0) {
            return brandRepository.findAll().associateBy { it.name }
        }


        val brands = listOf(
            Brand(name = "Michelin", isActive = true),
            Brand(name = "Continental", isActive = true),
            Brand(name = "Bridgestone", isActive = true),
            Brand(name = "Pirelli", isActive = true),
            Brand(name = "Goodyear", isActive = true)
        )

        val savedBrands = brandRepository.saveAll(brands)
        return savedBrands.associateBy { it.name }.toMutableMap()
    }
}

data class TyreData(
    val brand: String,
    val model: String,
    val width: Int,
    val height: Int,
    val diameter: Int,
    val type: String,
    val price: Double
)

