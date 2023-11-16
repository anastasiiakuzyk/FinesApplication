package ua.anastasiia.propertyautofill.bpp.fieldGeneration

interface RandomFieldGenerator {
    fun generate(): String
}

fun String.capitalizeFirstLetter() = this[0].uppercaseChar() + this.substring(1)
