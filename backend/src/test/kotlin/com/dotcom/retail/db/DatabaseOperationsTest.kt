package com.dotcom.retail.db

import com.dotcom.retail.common.BaseIntegrationTest
import com.dotcom.retail.domain.catalogue.brand.Brand
import com.dotcom.retail.domain.catalogue.brand.BrandRepository
import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.category.CategoryRepository
import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DatabaseOperationsTest : BaseIntegrationTest() {

    @Autowired lateinit var productRepository: ProductRepository
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var brandRepository: BrandRepository
    @Autowired lateinit var categoryRepository: CategoryRepository

    @BeforeEach
    fun seed() {
        productRepository.deleteAll()
        brandRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Nested
    inner class ProductPersistence {

        @Test
        fun `product is saved and retrieved by id`() {
            val saved = productRepository.save(
                Product(name = "Gadget", sku = "GDG-001", price = BigDecimal("99.99"), stock = 5)
            )
            val found = productRepository.findById(saved.id)
            assertTrue(found.isPresent)
            assertEquals("Gadget", found.get().name)
            assertEquals(BigDecimal("99.99"), found.get().price)
        }

        @Test
        fun `product is deleted and no longer retrievable`() {
            val saved = productRepository.save(
                Product(name = "Temp", sku = "TMP-001", price = BigDecimal("1.00"), stock = 1)
            )
            productRepository.deleteById(saved.id)
            assertFalse(productRepository.existsById(saved.id))
        }

        @Test
        fun `product fields are updated and persisted`() {
            val saved = productRepository.save(
                Product(name = "Old Name", sku = "SKU-UPD", price = BigDecimal("10.00"), stock = 3)
            )
            saved.name = "New Name"
            saved.price = BigDecimal("20.00")
            productRepository.save(saved)

            val updated = productRepository.findById(saved.id).get()
            assertEquals("New Name", updated.name)
            assertEquals(BigDecimal("20.00"), updated.price)
        }

        @Test
        fun `product with brand and category is persisted with relations`() {
            val brand = brandRepository.save(Brand(name = "Acme", isActive = true))
            val category = categoryRepository.save(Category(name = "Tools"))

            val product = productRepository.save(
                Product(
                    name = "Drill",
                    sku = "DRL-001",
                    price = BigDecimal("149.00"),
                    stock = 10,
                    brand = brand,
                    category = category,
                    isActive = true,
                )
            )

            val found = productRepository.findById(product.id).get()
            assertEquals(brand.id, found.brand?.id)
            assertEquals(category.id, found.category?.id)
        }

        @Test
        fun `findAll returns all persisted products`() {
            productRepository.saveAll(
                listOf(
                    Product(name = "P1", sku = "P1", price = BigDecimal.ONE, stock = 1),
                    Product(name = "P2", sku = "P2", price = BigDecimal.ONE, stock = 1),
                    Product(name = "P3", sku = "P3", price = BigDecimal.ONE, stock = 1),
                )
            )
            assertEquals(3, productRepository.count())
        }
    }

    @Nested
    inner class UserPersistence {

        @Test
        fun `user is saved and found by email`() {
            val user = userRepository.save(User(email = "db@test.com", displayName = "DB User"))
            val found = userRepository.findByEmail("db@test.com")
            assertNotNull(found)
            assertEquals(user.id, found!!.id)
        }

        @Test
        fun `user is deleted successfully`() {
            val user = userRepository.save(User(email = "delete@test.com", displayName = "Del User"))
            userRepository.deleteById(user.id)
            assertNull(userRepository.findByEmail("delete@test.com"))
        }

        @Test
        fun `user email is unique — duplicate save throws`() {
            userRepository.save(User(email = "unique@test.com", displayName = "First"))
            assertThrows(Exception::class.java) {
                userRepository.saveAndFlush(User(email = "unique@test.com", displayName = "Second"))
            }
        }
    }

    @Nested
    inner class BrandPersistence {

        @Test
        fun `brand is saved and retrieved`() {
            val brand = brandRepository.save(Brand(name = "BrandX", isActive = true))
            val found = brandRepository.findById(brand.id)
            assertTrue(found.isPresent)
            assertEquals("BrandX", found.get().name)
        }

        @Test
        fun `brand deletion removes it from DB`() {
            val brand = brandRepository.save(Brand(name = "Gone", isActive = false))
            brandRepository.deleteById(brand.id)
            assertFalse(brandRepository.existsById(brand.id))
        }
    }

    @Nested
    inner class CategoryPersistence {

        @Test
        fun `category is saved and retrieved by id`() {
            val category = categoryRepository.save(Category(name = "Electronics"))
            val found = categoryRepository.findById(category.id)
            assertTrue(found.isPresent)
            assertEquals("Electronics", found.get().name)
        }

        @Test
        fun `category is deleted and no longer retrievable`() {
            val category = categoryRepository.save(Category(name = "Disposable"))
            categoryRepository.deleteById(category.id)
            assertFalse(categoryRepository.existsById(category.id))
        }

        @Test
        fun `category name can be updated`() {
            val category = categoryRepository.save(Category(name = "Old"))
            category.name = "New"
            categoryRepository.save(category)
            assertEquals("New", categoryRepository.findById(category.id).get().name)
        }

        @Test
        fun `multiple categories are all persisted`() {
            categoryRepository.saveAll(listOf(Category(name = "A"), Category(name = "B"), Category(name = "C")))
            assertEquals(3, categoryRepository.count())
        }
    }
}
