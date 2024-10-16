plugins {
    kotlin("jvm") version "1.9.0"
    id("com.google.protobuf") version "0.9.4"
}

allprojects {
    group = "ua.anastasiia"
    version = "0.0.1-SNAPSHOT"
    repositories {
        mavenCentral()
        maven {
            setUrl("https://packages.confluent.io/maven/")
        }
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "com.google.protobuf")

    apply(plugin = "java")

    tasks.test {
        testLogging {
            showStandardStreams = true
        }
    }

    dependencies {
        implementation("com.google.api.grpc:proto-google-common-protos:2.26.0")

        implementation("io.projectreactor:reactor-core:3.5.11")

        implementation("io.grpc:grpc-core:1.59.0")
        implementation("io.grpc:grpc-protobuf:1.59.0")
        implementation("io.grpc:grpc-netty:1.59.0")
        implementation("io.grpc:grpc-stub:1.59.0")

        implementation("com.salesforce.servicelibs:reactor-grpc:1.2.4")
        implementation("com.salesforce.servicelibs:reactive-grpc-common:1.2.4")
        implementation("com.salesforce.servicelibs:reactor-grpc-stub:1.2.4")
        implementation("org.springframework.boot:spring-boot-starter-logging:3.3.0")
        implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    }
}
