package com.dotcom.retail.common

import com.dotcom.retail.testcontainers.PostgresContainerSingleton
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BaseIntegrationTest {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun cleanDatabase() {
        val tables = jdbcTemplate.queryForList(
            "SELECT tablename FROM pg_tables WHERE schemaname='public'",
            String::class.java
        )

        if (tables.isNotEmpty()) {
            val tableList = tables.joinToString(", ") { "\"$it\"" }
            jdbcTemplate.execute("TRUNCATE TABLE $tableList RESTART IDENTITY CASCADE")
        }
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", PostgresContainerSingleton::jdbcUrl)
            registry.add("spring.datasource.username", PostgresContainerSingleton::username)
            registry.add("spring.datasource.password", PostgresContainerSingleton::password)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
        }
    }
}