plugins {
    kotlin("jvm") version "1.9.0"
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.noarg") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

dependencies {
    implementation("com.graphhopper:graphhopper-core:9.1")
    implementation("com.graphhopper:graphhopper-reader-osm:3.0-pre3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.locationtech.jts:jts-core:1.18.2")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
