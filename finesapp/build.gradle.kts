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

    implementation("org.springframework.boot:spring-boot-starter-data-redis:3.1.5")

    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.nats:jnats:2.16.14")
    implementation("com.google.protobuf:protobuf-java:3.24.3")
    implementation(project(":internal-api"))

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.springframework.kafka:spring-kafka:3.0.12")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.21")
    implementation("io.confluent:kafka-schema-registry-maven-plugin:7.5.1")
    implementation("io.confluent:kafka-protobuf-serializer:7.5.1")

    implementation("net.devh:grpc-spring-boot-starter:2.15.0.RELEASE")
    implementation("net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation(project(":internal-api"))
    implementation(project(":finesapp:fine"))
}

subprojects {

    apply(plugin = "kotlin")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.plugin.noarg")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {

        implementation(project(":"))
        implementation(project(":internal-api"))

        implementation("org.glassfish.jersey.core:jersey-client:3.1.3")
        implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.3")
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("com.google.protobuf:protobuf-java-util:3.24.3")

        implementation("org.springframework.boot:spring-boot-starter-data-redis:3.1.5")

        implementation("jakarta.validation:jakarta.validation-api:3.0.2")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("io.nats:jnats:2.16.14")
        implementation("com.google.protobuf:protobuf-java:3.24.3")
        implementation(project(":internal-api"))

        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

        implementation("org.springframework.kafka:spring-kafka:3.0.12")
        implementation("io.projectreactor.kafka:reactor-kafka:1.3.21")
        implementation("io.confluent:kafka-schema-registry-maven-plugin:7.5.1")
        implementation("io.confluent:kafka-protobuf-serializer:7.5.1")

        implementation("net.devh:grpc-spring-boot-starter:2.15.0.RELEASE")
        implementation("net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
        enabled = false
    }

    tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
        enabled = false
    }
}

noArg {
    annotation("org.springframework.stereotype.Service")
}
tasks.withType<Test> {
    useJUnitPlatform()
}
