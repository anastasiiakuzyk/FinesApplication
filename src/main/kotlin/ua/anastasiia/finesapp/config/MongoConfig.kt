package ua.anastasiia.finesapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class MongoConfig {

    @Bean
    fun mongoTemplate(databaseFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(databaseFactory)
    }
}
