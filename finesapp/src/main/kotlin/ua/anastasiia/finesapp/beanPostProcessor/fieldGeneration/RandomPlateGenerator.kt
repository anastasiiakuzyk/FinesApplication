package ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration

class RandomPlateGenerator : RandomFieldGenerator {
    @Suppress("MagicNumber")
    override fun generate(): String {
        val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val plate = StringBuilder()

        repeat(2) {
            plate.append(uppercaseLetters.random())
        }

        repeat(4) {
            plate.append(numbers.random())
        }

        repeat(2) {
            plate.append(uppercaseLetters.random())
        }

        return plate.toString()
    }
}
