package com.dotcom.retail.product

import com.dotcom.retail.common.BaseIntegrationTest
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.catalogue.brand.BrandRepository
import com.dotcom.retail.domain.catalogue.category.CategoryRepository
import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProductControllerTest : BaseIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var productRepository: ProductRepository
    @Autowired lateinit var categoryRepository: CategoryRepository
    @Autowired lateinit var brandRepository: BrandRepository

    private lateinit var savedProduct: Product

    @BeforeEach
    fun seed() {
        productRepository.deleteAll()
        savedProduct = productRepository.save(
            Product(
                name = "Test Product",
                sku = "SKU-001",
                description = "A test product",
                price = BigDecimal("49.99"),
                salePrice = BigDecimal("39.99"),
                stock = 10,
                isActive = true,
            )
        )
    }

    @Test
    fun `getById returns 200 and product when product exists`() {
        mockMvc.perform(get("${ApiRoutes.Product.BASE}/{id}", savedProduct.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedProduct.id))
            .andExpect(jsonPath("$.name").value("Test Product"))
    }

    @Test
    fun `getById returns 404 when product does not exist`() {
        mockMvc.perform(get("${ApiRoutes.Product.BASE}/{id}", 999999L))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getProducts returns 200 with paged response`() {
        mockMvc.perform(get(ApiRoutes.Product.BASE))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.page").exists())
    }

    @Test
    fun `getProducts returns only active products`() {
        productRepository.save(
            Product(name = "Inactive", sku = "SKU-OFF", price = BigDecimal("5.00"), stock = 0, isActive = false)
        )

        mockMvc.perform(get(ApiRoutes.Product.BASE))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `search returns 200 with matching products`() {
        mockMvc.perform(
            get(ApiRoutes.Product.BASE)
                .param("search", "Test")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.page").exists())
    }

    @Test
    fun `search returns empty results for non-matching query`() {
        mockMvc.perform(
            get(ApiRoutes.Product.BASE)
                .param("search", "zzznomatch")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(0))
    }

    @Test
    fun `search returns 400 when query param is missing`() {
        mockMvc.perform(get(ApiRoutes.Product.BASE + ApiRoutes.Product.SEARCH))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create returns 201 and persists product`() {
        val dto = mapOf(
            "name" to "New Product",
            "sku" to "SKU-NEW",
            "price" to 29.99,
            "salePrice" to 19.99,
            "stock" to 5,
            "isActive" to true
        )

        val productPart = MockMultipartFile(
            "product", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(dto)
        )

        val imagePart = MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image".toByteArray())

        mockMvc.perform(
            multipart(ApiRoutes.Admin.Product.BASE)
                .file(productPart)
                .file(imagePart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("New Product"))
            .andExpect(jsonPath("$.id").exists())
    }

    @Test
    fun `create returns 400 when required fields are missing`() {
        val invalidDto = mapOf("description" to "no name or sku")

        val productPart = MockMultipartFile(
            "product", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(invalidDto)
        )

        mockMvc.perform(
            multipart(ApiRoutes.Admin.Product.BASE)
                .file(productPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `edit returns 200 with updated product`() {
        val patch = mapOf(
            "id" to savedProduct.id,
            "name" to "Updated Name",
            "sku" to "SKU-001",
            "price" to 59.99,
            "salePrice" to 49.99,
            "stock" to 8,
            "isActive" to true
        )

        val productPart = MockMultipartFile(
            "product", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(patch)
        )

        mockMvc.perform(
            multipart("${ApiRoutes.Admin.Product.BASE}/{id}", savedProduct.id)
                .file(productPart)
                .with { it.method = "PATCH"; it }
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated Name"))
    }

    @Test
    fun `delete returns 204 and removes product`() {
        mockMvc.perform(delete("${ApiRoutes.Admin.Product.BASE}/{id}", savedProduct.id))
            .andExpect(status().isNoContent)

        assertFalse(productRepository.existsById(savedProduct.id))
    }

    @Test
    fun `delete returns 404 when product does not exist`() {
        mockMvc.perform(delete("${ApiRoutes.Admin.Product.BASE}/{id}", 999999L))
            .andExpect(status().isNotFound)
    }
}