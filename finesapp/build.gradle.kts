plugins {
    kotlin("jvm") version "1.9.0"
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.noarg") version "1.9.0"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-web")
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

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("io.projectreactor:reactor-test:3.6.4")
    testImplementation("com.ninja-squad:springmockk:4.0.2")

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
        implementation(project(":property-autofill"))

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
        implementation(project(":fine-generation"))
        implementation("software.amazon.awssdk:s3:2.28.22")
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
