import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.4.13"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"  // Torna classes @Component, @Service, etc. open
    kotlin("plugin.jpa") version "2.1.20"     // Gera construtores no-arg para @Entity
}

group = "com.rodojacto"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // ─── Spring Boot ─────────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ─── Kotlin ──────────────────────────────────────────────
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // ─── Banco de Dados ──────────────────────────────────────
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")

    // ─── Flyway ──────────────────────────────────────────────
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // ─── JWT (jjwt 0.12.x — API moderna) ─────────────────────
    implementation("io.jsonwebtoken:jjwt-api:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.7")

    // ─── OpenAPI / Swagger UI ────────────────────────────────
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17")

    // ─── Testes ──────────────────────────────────────────────
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
