package com.dotcom.retail.filter

import com.dotcom.retail.common.BaseIntegrationTest
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.catalogue.filter.Filter
import com.dotcom.retail.domain.catalogue.filter.FilterService
import com.dotcom.retail.domain.catalogue.filter.RangeData
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FilterControllerTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var filterService: FilterService

    @Test
    fun `GET filters should return 200 and correct structure`() {
        val categoryId = 1L
        val mockResponse = Filter(
            categoryId = categoryId,
            attributes = emptyList(),
            brands = emptyList(),
            price = RangeData(0.0, 100.0)
        )

        every { filterService.getFilters(categoryId) } returns mockResponse

        mockMvc.get(ApiRoutes.Filter.BASE) {
            param("categoryId", categoryId.toString())
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.categoryId") { value(categoryId) }
            jsonPath("$.price.max") { value(100.0) }
        }
    }

    @Test
    fun `GET filters should return 400 Bad Request when parameter is missing`() {
        mockMvc.get(ApiRoutes.Filter.BASE) {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }
    }
}