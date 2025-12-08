package com.dotcom.retail

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class DotComRetailApplication

fun main(args: Array<String>) {
	runApplication<DotComRetailApplication>(*args)
}
