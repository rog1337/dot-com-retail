import org.springframework.boot.gradle.tasks.run.BootRun
import java.util.Properties

tasks.named<BootRun>("bootRun") {
	val props = Properties()
	file("../.env").reader().use { props.load(it) }
	val keys = props.keys.map { it.toString() }.toSet()
	environment = keys.associateWith { props.getProperty(it) }
}

plugins {
	kotlin("jvm") version "2.0.20"
	kotlin("plugin.spring") version "2.0.20"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.0.20"
}

group = "com.dotcom"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.security:spring-security-oauth2-client:6.5.6")
    implementation("org.springframework.security:spring-security-oauth2-jose:6.5.6")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("com.google.zxing:core:3.5.4")
	implementation("com.google.zxing:javase:3.5.4")
	implementation("dev.samstevens.totp:totp:1.7.1")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.15")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("com.stripe:stripe-java:31.3.0")
	implementation("org.openapitools:jackson-databind-nullable:0.2.9")
	implementation("net.coobird:thumbnailator:0.4.21")
	implementation("org.sejda.imageio:webp-imageio:0.1.6")
	implementation("com.bucket4j:bucket4j_jdk17-core:8.17.0")
	implementation("io.lettuce:lettuce-core")
	implementation("com.bucket4j:bucket4j_jdk17-lettuce:8.17.0")
	developmentOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.15")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:junit-jupiter")
    testImplementation(kotlin("test"))
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.postgresql:postgresql")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperties["api.version"] = "1.44"
}