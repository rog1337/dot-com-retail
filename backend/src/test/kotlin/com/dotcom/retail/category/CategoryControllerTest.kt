package com.dotcom.retail.category

import com.dotcom.retail.common.BaseIntegrationTest
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.admin.category.dto.CreateCategoryRequest
import com.dotcom.retail.domain.admin.category.dto.EditCategoryRequest
import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.category.CategoryRepository
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttribute
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeRepository
import com.dotcom.retail.domain.catalogue.category.attribute.FilterType
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest : BaseIntegrationTest() {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var categoryRepository: CategoryRepository
    @Autowired private lateinit var attributeRepository: CategoryAttributeRepository
    @Autowired private lateinit var productRepository: ProductRepository

    @BeforeEach
    fun setup() {
        productRepository.deleteAll()
        categoryRepository.deleteAll()
        attributeRepository.deleteAll()
    }

    @Test
    fun `get should return 200 and category details`() {
        val category = categoryRepository.save(Category(name = "Test"))

        mockMvc.perform(get("${ApiRoutes.Category.BASE}/{id}", category.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(category.id))
            .andExpect(jsonPath("$.name").value(category.name))
    }

    @Test
    fun `create should return 201 and persist category`() {
        val request = CreateCategoryRequest(name = "Test", attributeIds = emptyList())

        mockMvc.perform(
            post(ApiRoutes.Admin.Category.BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value(request.name))

        val stored = categoryRepository.findAll().first()
        assertEquals(request.name, stored.name)
    }

    @Test
    fun `create should link attributes correctly`() {
        val attr = attributeRepository.save(
            CategoryAttribute(
                attribute = "test",
                label = "Test",
                dataType = AttributeDataType.NUMBER,
                filterType = FilterType.CHECKBOX,
                displayOrder = 1,
                isPublic = true
            )
        )

        val request = CreateCategoryRequest(name = "Test", attributeIds = listOf(attr.id))

        mockMvc.perform(
            post(ApiRoutes.Admin.Category.BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        val child = categoryRepository.findAll().find { it.name == "Test" }
        assertNotNull(child)
        assertEquals(1, child!!.attributes.size)
        assertEquals("test", child.attributes[0].attribute)
    }

    @Test
    fun `edit should update name and attributes`() {
        val category = categoryRepository.save(Category(name = "old name"))
        val attr = attributeRepository.save(
            CategoryAttribute(
                attribute = "test",
                label = "Test",
                dataType = AttributeDataType.TEXT,
                filterType = FilterType.CHECKBOX,
                displayOrder = 1,
                isPublic = true
            )
        )

        val updateRequest = EditCategoryRequest(id = category.id, name = "new name", attributeIds = listOf(attr.id))

        mockMvc.perform(
            put("${ApiRoutes.Admin.Category.BASE}/{id}", category.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("new name"))

        val updated = categoryRepository.findById(category.id).get()
        assertEquals("new name", updated.name)
        assertTrue(updated.attributes.isNotEmpty())
    }

    @Test
    fun `delete should return 204 and remove category`() {
        val category = categoryRepository.save(Category(name = "test"))

        mockMvc.perform(delete("${ApiRoutes.Admin.Category.BASE}/{id}", category.id))
            .andExpect(status().isNoContent)

        assertFalse(categoryRepository.existsById(category.id))
    }
}