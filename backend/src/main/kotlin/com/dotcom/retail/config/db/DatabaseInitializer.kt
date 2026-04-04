package com.dotcom.retail.config.db

import com.dotcom.retail.config.properties.FileProperties
import com.dotcom.retail.domain.catalogue.brand.Brand
import com.dotcom.retail.domain.catalogue.brand.BrandRepository
import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.category.CategoryRepository
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeMetadataService
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttribute
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeRepository
import com.dotcom.retail.domain.catalogue.category.attribute.FilterType
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.domain.catalogue.image.ImageMetadata
import com.dotcom.retail.domain.catalogue.image.ImageRepository
import com.dotcom.retail.domain.catalogue.image.ImageService
import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import com.dotcom.retail.domain.catalogue.review.Review
import com.dotcom.retail.domain.catalogue.review.ReviewRepository
import com.dotcom.retail.domain.catalogue.review.ReviewVote
import com.dotcom.retail.domain.user.Role
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.file.Files
import java.util.*
import javax.imageio.ImageIO
import javax.sql.DataSource
import kotlin.math.roundToLong
import kotlin.random.Random

@Component
class DatabaseInitializer(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val fileProperties: FileProperties,
    private val imageRepository: ImageRepository,
    private val categoryAttributeRepository: CategoryAttributeRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val dataSource: DataSource,
    private val imageService: ImageService,
    @Value("\${app.db.init.product-count}") private val targetProductCount: Int,
    @Value("\${app.db.init.user-count}") private val targetUserCount: Int,
    private val reviewRepository: ReviewRepository,
    private val attributeMetadataService: AttributeMetadataService,
) : CommandLineRunner {

    val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    override fun run(vararg args: String?) {
        executeInitScript()
        generateData()
    }

    fun executeInitScript() {
        val resource = PathMatchingResourcePatternResolver().getResource("classpath:db/migration/start_script.sql")
        val populator = ResourceDatabasePopulator()
        populator.addScript(resource)
        populator.setSeparator(ScriptUtils.EOF_STATEMENT_SEPARATOR)
        populator.setContinueOnError(false)
        populator.setIgnoreFailedDrops(true)
        populator.setSqlScriptEncoding("UTF-8")
        try {
            DatabasePopulatorUtils.execute(populator, dataSource)
        } catch (e: Exception) {
            logger.error("Error executing init script: {}", e)
        }
    }

    fun generateData() {
        val currentCount = productRepository.count()
        if (currentCount >= targetProductCount) {
            logger.info("Database already contains $currentCount products. Target is $targetProductCount. Skipping generation.")
            return
        }

        logger.info("Generating data to reach $targetProductCount products...")

        val users = initUsers()
        val brandsMap = initBrands()
        val categoriesMap = initCategories()
        val imagesMap = initImages()

        val productsToGenerate = targetProductCount - currentCount.toInt()

        val productsBatch = mutableListOf<Product>()
        val batchSize = 100

        for (i in 1..productsToGenerate) {
            productsBatch.add(generateRandomProduct(brandsMap, categoriesMap, imagesMap))

            if (i % batchSize == 0 || i == productsToGenerate) {
                val savedProducts = productRepository.saveAll(productsBatch)
                generateReviews(savedProducts, users)

                logger.info("Saved batch of ${productsBatch.size}. Total generated so far: $i / $productsToGenerate")
                productsBatch.clear()
            }
        }

        attributeMetadataService.refresh()
        logger.info("Database seeding complete!")
    }

    fun initUsers(): List<User> {
        val existingUsers = userRepository.findAll()
        if (existingUsers.size > 3) return existingUsers

        logger.info("Generating $targetUserCount dummy users...")
        val users = (1..targetUserCount).map {
            User(
                email = "test$it@example.com",
                displayName = "Tester $it",
                passwordHash = "dummyhash",
                role = Role.USER
            )
        }
        return userRepository.saveAll(users)
    }

    fun initImages(): Map<String, Image> {
        val requiredFiles = listOf(
            "sample_michelin.png", "sample_continental.png", "sample_goodyear.png",
            "sample_pirelli.png", "sample_bridgestone.png", "sample_wunderbaum.png",
            "sample_tyre_repair_kit.webp"
        )

        val imageCache = mutableMapOf<String, Image>()
        val path = fileProperties.productPathFull

        requiredFiles.forEach { fileName ->
            val file = path.resolve("sample/$fileName")
            if (Files.exists(file)) {
                val original = ImageIO.read(file.toFile())
                val meta = ImageMetadata(fileName = "", sortOrder = 0, altText = "Sample $fileName")

                val savedImage = imageService.create(original, Files.probeContentType(file), meta, fileProperties.productPath)
                imageCache[fileName] = savedImage
            } else {
                logger.warn("Sample image $fileName missing, skipping cache for this file.")
            }
        }

        return imageCache
    }

    fun generateReviews(products: List<Product>, users: List<User>) {
        if (users.isEmpty()) return

        val reviewsBatch = mutableListOf<Review>()

        products.forEach { product ->
            if (Random.nextInt(100) < 60) {
                val numReviews = Random.nextInt(1, 8)

                for (i in 1..numReviews) {
                    val reviewer = users.random()
                    val review = Review(
                        product = product,
                        user = reviewer,
                        rating = Random.nextInt(1, 6),
                        body = "Load testing auto-generated review. Works great!"
                    )

                    if (Random.nextInt(100) < 40) {
                        val numVotes = Random.nextInt(1, 5)
                        for (v in 1..numVotes) {
                            val voter = users.random()
                            review.votes.add(ReviewVote(review = review, user = voter))
                        }
                    }
                    val newTotalScore = (product.averageRating * product.reviewCount) + review.rating
                    product.apply {
                        reviewCount = numReviews
                        averageRating = ((newTotalScore / numReviews) * 10).roundToLong() / 10.0
                    }
                    reviewsBatch.add(review)
                }
            }
        }

        if (reviewsBatch.isNotEmpty()) {
            reviewRepository.saveAll(reviewsBatch)
            productRepository.saveAll(products)
        }
    }

    fun initCategories(): Map<String, Category> {
        val existing = categoryRepository.findAll().associateBy { it.name }
        if (existing.isNotEmpty()) return existing

        val tyreAttrs = listOf(
            CategoryAttribute(attribute = "diameter", label = "Diameter", dataType = AttributeDataType.NUMBER, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0),
            CategoryAttribute(attribute = "type", label = "Type", dataType = AttributeDataType.TEXT, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0),
            CategoryAttribute(attribute = "width", label = "Width", dataType = AttributeDataType.NUMBER, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0),
            CategoryAttribute(attribute = "height", label = "Height", dataType = AttributeDataType.NUMBER, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0)
        )
        val airFreshenerAttrs = listOf(
            CategoryAttribute(attribute = "scent", label = "Scent", dataType = AttributeDataType.TEXT, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0)
        )
        val tyreRepairAttrs = listOf(
            CategoryAttribute(attribute = "type", label = "Type", dataType = AttributeDataType.TEXT, filterType = FilterType.CHECKBOX, isPublic = true, displayOrder = 0)
        )

        categoryAttributeRepository.saveAll(tyreAttrs + airFreshenerAttrs + tyreRepairAttrs)

        val categories = listOf(
            Category(name = "Tyres", attributes = tyreAttrs.toMutableList()),
            Category(name = "Air Fresheners", attributes = airFreshenerAttrs.toMutableList()),
            Category(name = "Tyre Repair Kits", attributes = tyreRepairAttrs.toMutableList())
        )

        return categoryRepository.saveAll(categories).associateBy { it.name }
    }

    fun initBrands(): Map<String, Brand> {
        val existing = brandRepository.findAll().associateBy { it.name }
        if (existing.isNotEmpty()) return existing

        val brands = listOf(
            Brand(name = "Michelin", isActive = true),
            Brand(name = "Continental", isActive = true),
            Brand(name = "Bridgestone", isActive = true),
            Brand(name = "Pirelli", isActive = true),
            Brand(name = "Goodyear", isActive = true),
            Brand(name = "Wunderbaum", isActive = true),
            Brand(name = "Slime", isActive = true)
        )

        return brandRepository.saveAll(brands).associateBy { it.name }
    }

    fun generateRandomProduct(
        brands: Map<String, Brand>,
        categories: Map<String, Category>,
        imagesMap: Map<String, Image>
    ): Product {
        val rand = Random.nextInt(100)

        val categoryName = when {
            rand < 70 -> "Tyres"
            rand < 85 -> "Air Fresheners"
            else -> "Tyre Repair Kits"
        }

        val category = categories[categoryName] ?: throw RuntimeException("Category $categoryName missing")

        val brand = when (categoryName) {
            "Air Fresheners" -> brands["Wunderbaum"]!!
            "Tyre Repair Kits" -> brands["Slime"]!!
            else -> listOf(brands["Michelin"]!!, brands["Continental"]!!, brands["Bridgestone"]!!, brands["Pirelli"]!!, brands["Goodyear"]!!).random()
        }

        val fileName = when (categoryName) {
            "Tyres" -> "sample_${brand.name.lowercase()}.png"
            "Air Fresheners" -> "sample_wunderbaum.png"
            "Tyre Repair Kits" -> "sample_tyre_repair_kit.webp"
            else -> "sample_michelin.png"
        }

        val imageCache = imagesMap[fileName]
        val imageList = imageCache?.let {
            val image = Image(
                id = 0,
                fileName = it.fileName,
                contentType = it.contentType,
                sortOrder = it.sortOrder,
                altText = it.altText,
            )
            mutableListOf(image)
        } ?: mutableListOf()

        if (imageList.isNotEmpty()) {
            imageRepository.saveAll(imageList)
        }

        var productName = ""
        var price = 0.0
        val attributes = mutableMapOf<String, MutableList<Any>>()

        when (categoryName) {
            "Tyres" -> {
                val models = listOf(
                    "Michelin Pilot Sport 4",
                    "Michelin Pilot Sport 4S",
                    "Michelin Pilot Sport 5",
                    "Michelin Primacy 4",
                    "Michelin Primacy 4+",
                    "Michelin CrossClimate 2",
                    "Michelin CrossClimate+",
                    "Michelin Latitude Sport 3",
                    "Michelin Alpin 6",
                    "Michelin X-Ice Snow",

                    "Continental PremiumContact 6",
                    "Continental PremiumContact 7",
                    "Continental EcoContact 6",
                    "Continental SportContact 6",
                    "Continental SportContact 7",
                    "Continental AllSeasonContact",
                    "Continental AllSeasonContact 2",
                    "Continental WinterContact TS 870",
                    "Continental WinterContact TS 860",
                    "Continental CrossContact LX25",

                    "Pirelli P Zero",
                    "Pirelli P Zero PZ4",
                    "Pirelli Cinturato P7",
                    "Pirelli Cinturato P7 All Season Plus",
                    "Pirelli Scorpion Verde",
                    "Pirelli Scorpion All Season Plus 3",
                    "Pirelli Scorpion Winter",
                    "Pirelli Winter Sottozero 3",

                    "Bridgestone Potenza Sport",
                    "Bridgestone Potenza RE980AS",
                    "Bridgestone Turanza T005",
                    "Bridgestone Turanza QuietTrack",
                    "Bridgestone Ecopia EP422 Plus",
                    "Bridgestone Blizzak WS90",
                    "Bridgestone Dueler H/L Alenza Plus",

                    "Goodyear Eagle F1 Asymmetric 6",
                    "Goodyear Eagle F1 Asymmetric 5",
                    "Goodyear Eagle F1 SuperSport",
                    "Goodyear EfficientGrip Performance 2",
                    "Goodyear EfficientGrip SUV",
                    "Goodyear Vector 4Seasons Gen-3",
                    "Goodyear UltraGrip Performance+",
                    "Goodyear Wrangler All-Terrain Adventure"
                )
                val widths = listOf(195, 205, 215, 225, 235, 245, 255, 265, 275)
                val heights = listOf(35, 40, 45, 50, 55, 60, 65)
                val diameters = listOf(15, 16, 17, 18, 19, 20)
                val types = listOf("Summer", "Winter", "All-Season")

                val width = widths.random()
                val height = heights.random()
                val diameter = diameters.random()
                val type = types.random()
                val model = models.random()

                productName = "${brand.name} $model $width/$height R$diameter"
                price = Random.nextDouble(60.0, 350.0)

                attributes["width"] = mutableListOf(width)
                attributes["height"] = mutableListOf(height)
                attributes["diameter"] = mutableListOf(diameter)
                attributes["type"] = mutableListOf(type)
            }
            "Air Fresheners" -> {
                val scents = listOf("Pine", "Vanilla", "New Car", "Lemon", "Black Classic")
                val scent = scents.random()

                productName = "${brand.name} Air Freshener - $scent"
                price = Random.nextDouble(2.5, 8.0)
                attributes["scent"] = mutableListOf(scent)
            }
            "Tyre Repair Kits" -> {
                productName = "${brand.name} Tyre Repair Kit ${Random.nextInt(1, 5)}L"
                price = Random.nextDouble(20.0, 95.0)
                attributes["type"] = mutableListOf("Repair Kit")
            }
        }

        val priceBd = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP)
        val salePriceBd = priceBd.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP)

        return Product(
            name = productName,
            sku = "PRD-${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
            description = "Premium quality $productName.",
            price = priceBd,
            salePrice = salePriceBd,
            stock = Random.nextInt(5, 500),
            isActive = true,
            brand = brand,
            attributes = attributes,
            images = imageList,
            category = category,
        )
    }
}