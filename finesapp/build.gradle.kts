plugins {
    kotlin("jvm") version "1.9.0"
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.jpa") version "1.9.0"
    kotlin("plugin.noarg") version "1.9.0"
}

dependencies {
    implementation("org.glassfish.jersey.core:jersey-client:3.1.3")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.3")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.google.protobuf:protobuf-java-util:3.24.3")

    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.nats:jnats:2.16.14")
    implementation("com.google.protobuf:protobuf-java:3.24.3")
    implementation(project(":internal-api"))

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

noArg {
    annotation("org.springframework.stereotype.Service")
}
tasks.withType<Test> {
    useJUnitPlatform()
}
