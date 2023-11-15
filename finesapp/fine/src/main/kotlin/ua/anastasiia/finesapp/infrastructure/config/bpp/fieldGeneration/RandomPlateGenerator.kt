package ua.anastasiia.finesapp.infrastructure.config.bpp.fieldGeneration

class RandomPlateGenerator : RandomFieldGenerator {
    companion object {
        private const val LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val NUMBERS = "0123456789"
        private const val NUMBER_OF_LETTERS = 2
        private const val NUMBER_NUMBERS = 4
    }

    override fun generate(): String {
        return StringBuilder().apply {
            repeat(NUMBER_OF_LETTERS) { append(LETTERS.random()) }
            repeat(NUMBER_NUMBERS) { append(NUMBERS.random()) }
            repeat(NUMBER_OF_LETTERS) { append(LETTERS.random()) }
        }.toString()
    }
}
