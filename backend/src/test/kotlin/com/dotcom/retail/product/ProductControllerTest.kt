package com.dotcom.retail.product

import com.dotcom.retail.common.BaseIntegrationTest
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.catalogue.product.*
import com.dotcom.retail.security.jwt.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.servlet.function.RequestPredicates.contentType
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest : BaseIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jwtService: JwtService

    @SpykBean lateinit var productService: ProductService
    @Autowired private lateinit var productRepository: ProductRepository

    @Autowired lateinit var productMapper: ProductMapper

    var product = Product(
        id = 0,
        name = "testProduct",
        sku = "testSku",
        description = "testDescription",
        price = BigDecimal(10),
        salePrice = BigDecimal(10),
        stock = 1,
        brand = null,
        category = null,
        images = mutableListOf(),
        attributes = mutableMapOf(),
        isActive = true,
        searchContent = null,
    )

    var createProduct = CreateProduct(
        name = "testProduct",
        sku = "testSku",
        price = BigDecimal(10),
        salePrice = BigDecimal(10),
        stock = 1,
        description = null,
        brandId = null,
        categoryId = null,
        images = null,
        attributes = listOf(),
        isActive = true,
    )

    var editProduct = EditProductDto(
        id = 1,
        name = JsonNullable.of("editProduct"),
        sku = JsonNullable.of("sku"),
        description = JsonNullable.of("edited product"),
        price = JsonNullable.of(BigDecimal(10)),
        salePrice = JsonNullable.of(BigDecimal(10)),
        stock = JsonNullable.of(1),
        brandId = JsonNullable.of(null),
        categoryId = JsonNullable.of(null),
        images = JsonNullable.of(null),
        attributes = JsonNullable.of(null),
        isActive = JsonNullable.of(true),
    )

    @BeforeEach
    fun seed() {
        productRepository.deleteAll()
        product = productRepository.save(product)
    }

    @Test
    fun `create should return 201 with created product`() {
        val product = createProduct.copy()

        val productJson = objectMapper.writeValueAsString(product)
        val productPart = MockMultipartFile("product", "", MediaType.APPLICATION_JSON_VALUE, productJson.toByteArray())

        val imagePart = MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image".toByteArray())
        mockMvc.perform(
            multipart(ApiRoutes.Product.BASE)
                .file(productPart)
                .file(imagePart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect {
                status().isCreated
                content().contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.id").exists()
                assertTrue(productRepository.existsById(JsonPath.read<Long>(it.response.contentAsString, "$.id")))
            }
    }

    @Test
    fun `getProducts returns paged response`() {
        val params = ProductQuery(1)

        mockMvc.get(
            ApiRoutes.Product.BASE,
            contentType(MediaType.APPLICATION_JSON), params
        )
            .andExpect {status().isOk}
            .andExpect {jsonPath("$.data[0].id").exists()}
            .andExpect {jsonPath("$.page").exists()}
    }

    @Test
    fun `getById returns 200 and product`() {
        mockMvc.perform(get("${ApiRoutes.Product.BASE}/{id}", product.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
    }


    @Test
    fun `edit returns 200 with edited product`() {
        val editJson = objectMapper.writeValueAsString(editProduct)

        val productPart = MockMultipartFile(
            "product",
            "",
            "application/json",
            editJson.toByteArray()
        )

        mockMvc.perform(multipart("${ApiRoutes.Product.BASE}/{id}", product.id)
            .file(productPart)
            .with { it.method = "PATCH"; it })

            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").exists())
    }

    @Test
    fun `search returns 200 with paged products`() {
        val query = "tyre"
        val pageable = PageRequest.of(0, 10)
        val productPage = PageImpl(listOf(product))

        mockMvc.perform(get(ApiRoutes.Product.BASE + ApiRoutes.Product.SEARCH)
            .param("query", query))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.page").exists())
    }

    @Test
    fun `delete returns 204 no content`() {
        mockMvc.perform(delete("${ApiRoutes.Product.BASE}/{id}", product.id))
            .andExpect(status().isNoContent)

        assertFalse(productRepository.existsById(product.id))
    }
}