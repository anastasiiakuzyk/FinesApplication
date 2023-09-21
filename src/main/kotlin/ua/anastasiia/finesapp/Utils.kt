package ua.anastasiia.finesapp

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
