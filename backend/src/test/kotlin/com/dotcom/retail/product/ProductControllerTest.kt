package com.dotcom.retail.product

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.config.properties.JwtProperties
import com.dotcom.retail.domain.catalogue.product.*
import com.dotcom.retail.security.jwt.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.servlet.function.RequestPredicates.contentType
import java.math.BigDecimal

@WebMvcTest(ProductController::class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @MockkBean lateinit var jwtService: JwtService

    @MockkBean(relaxed = true) lateinit var jwtProperties: JwtProperties

    @MockkBean(relaxed = true) lateinit var productService: ProductService
    @MockkBean(relaxed = true) lateinit var productMapper: ProductMapper
    @MockkBean lateinit var productRepository: ProductRepository

    val product = Product(
        id = 1,
        name = "testProduct",
        sku = "testSku",
        description = "testDescription",
        price = BigDecimal(10),
        salePrice = BigDecimal(10),
        stock = 1,
        brand = null,
        category = null,
        images = mutableListOf(),
        attributes = null,
        isActive = true,
        searchContent = null,
    )

    val createProduct = CreateProduct(
        name = "testProduct",
        sku = "testSku",
        price = BigDecimal(10),
        salePrice = BigDecimal(10),
        stock = 1,
        description = null,
        brandId = null,
        categoryId = null,
        images = null,
        attributes = null,
        isActive = true,
    )

    val editProduct = EditProductDto(
        id = 1,
        name = "editProduct",
        sku = "sku",
        description = "edited product",
        price = BigDecimal(10),
        salePrice = BigDecimal(10),
        stock = 1,
        brandId = null,
        categoryId = null,
        images = null,
        attributes = null,
        isActive = true,
    )

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
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
    }

    @Test
    fun `getProducts returns paged response`() {
        val params = ProductQueryParams(1)

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
        val editRequest = editProduct.copy()
        val editJson = objectMapper.writeValueAsString(editRequest)

        val productPart = MockMultipartFile(
            "product",
            "",
            "application/json",
            editJson.toByteArray()
        )

        mockMvc.perform(multipart("${ApiRoutes.Product.BASE}/{id}", product.id)
            .file(productPart)
            .with { it.method = "PUT"; it })

            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").exists())
    }

    @Test
    fun `getImage returns 200 with resource and correct content type`() {
        val imageId = 1
        val mockResource = ByteArrayResource("image-data".toByteArray())

        every { productService.getImage(1, 1) } returns mockResource

        val url = "${ApiRoutes.Product.BASE}/1${ApiRoutes.Product.IMAGE}/$imageId"

        mockMvc.perform(get(url))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes("image-data".toByteArray()))
    }

    @Test
    fun `search returns 200 with paged products`() {
        val query = "tyre"
        val pageable = PageRequest.of(0, 10)
        val productPage = PageImpl(listOf(product))

        every { productRepository.searchByText(query, pageable) } returns productPage

        mockMvc.perform(get(ApiRoutes.Product.BASE + ApiRoutes.Product.SEARCH)
            .param("query", query))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.page").exists())
    }

    @Test
    fun `delete returns 204 no content`() {
        every { productService.delete(product.id) } just runs

        mockMvc.perform(delete("${ApiRoutes.Product.BASE}/{id}", product.id))
            .andExpect(status().isNoContent)

        verify(exactly = 1) { productService.delete(product.id) }
    }
}