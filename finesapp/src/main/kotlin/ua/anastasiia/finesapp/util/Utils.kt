package ua.anastasiia.finesapp.util

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.UpdateDefinition
import reactor.core.publisher.Mono

fun String.capitalizeFirstLetter() = this[0].uppercaseChar() + this.substring(1)

inline fun <reified T : Any> ReactiveMongoOperations.findAndModify(query: Query, update: UpdateDefinition): Mono<T> =
    findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), T::class.java)

inline fun <reified T : Any> ReactiveMongoOperations.findAndRemove(query: Query): Mono<T> =
    findAndRemove(query, T::class.java)
