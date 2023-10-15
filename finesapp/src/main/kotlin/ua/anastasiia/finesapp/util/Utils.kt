package ua.anastasiia.finesapp.util

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.UpdateDefinition
import kotlin.random.Random

fun StringBuilder.shuffle() {
    val charList = this.toString().toMutableList()
    charList.shuffle(Random.Default)
    this.clear()
    for (ch in charList) {
        this.append(ch)
    }
}

fun String.capitalizeFirstLetter() = this[0].uppercaseChar() + this.substring(1)

inline fun <reified T : Any> MongoOperations.findAndModify(query: Query, update: UpdateDefinition): T? =
    findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), T::class.java)

inline fun <reified T : Any> MongoOperations.findAndRemove(query: Query): T? =
    findAndRemove(query, T::class.java)
