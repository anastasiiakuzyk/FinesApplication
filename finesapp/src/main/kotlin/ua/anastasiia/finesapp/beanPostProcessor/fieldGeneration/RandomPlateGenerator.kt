package ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration

class RandomPlateGenerator : RandomFieldGenerator {
    private val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numbers = "0123456789"
    private val numberOfLetters = 2

    @Suppress("MagicNumber")
    private val numberOfNumbers = 4

    override fun generate(): String {
        return StringBuilder().apply {
            repeat(numberOfLetters) { append(uppercaseLetters.random()) }
            repeat(numberOfNumbers) { append(numbers.random()) }
            repeat(numberOfLetters) { append(uppercaseLetters.random()) }
        }.toString()
    }
}
