package ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration

import ua.anastasiia.finesapp.util.shuffle
import kotlin.random.Random

class RandomPlateGenerator : RandomFieldGenerator {
    @Suppress("MagicNumber")
    override fun generate(): String {
        val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZАБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ"
        val numbers = "0123456789"
        val plate = StringBuilder().apply {
            append(uppercaseLetters.random())
            append(uppercaseLetters.random())
        }
        val randomLength = Random.nextInt(1, 7)
        val allChars = "$uppercaseLetters$numbers"

        repeat(randomLength) {
            plate.append(allChars.random())
        }
        plate.shuffle()

        return plate.toString()
    }
}
