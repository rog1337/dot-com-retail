package com.dotcom.retail.testcontainers

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

object PostgresContainerSingleton {
    val instance: PostgreSQLContainer<*> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)
            .also { it.start() }
    }

    fun jdbcUrl() = instance.jdbcUrl
    fun username() = instance.username
    fun password() = instance.password
}