package ua.anastasiia.finesapp.generation

class RandomPlateGenerator : RandomFieldGenerator<String> {

    override fun generate(number: Int): List<String> {
        return List(number) {
            StringBuilder().apply {
                repeat(NUMBER_OF_LETTERS) { append(LETTERS.random()) }
                repeat(NUMBER_NUMBERS) { append(NUMBERS.random()) }
                repeat(NUMBER_OF_LETTERS) { append(LETTERS.random()) }
            }.toString()
        }
    }

    companion object {
        private const val LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val NUMBERS = "0123456789"
        private const val NUMBER_OF_LETTERS = 2
        private const val NUMBER_NUMBERS = 4
    }
}
