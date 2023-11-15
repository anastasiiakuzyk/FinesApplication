package ua.anastasiia.finesapp.infrastructure.config.bpp.fieldGeneration

interface RandomFieldGenerator {
    fun generate(): String
}

fun String.capitalizeFirstLetter() = this[0].uppercaseChar() + this.substring(1)
