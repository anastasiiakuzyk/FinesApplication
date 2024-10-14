package ua.anastasiia.finesapp.generation

import ua.anastasiia.finesapp.entity.Colors
import kotlin.random.Random

class RandomColorGenerator : RandomFieldGenerator<String> {

    override fun generate(number: Int): List<String> {
        val colorsSize = Colors.entries.size
        return List(number) {
            Colors.entries[Random.nextInt(colorsSize)].name
        }
    }
}
