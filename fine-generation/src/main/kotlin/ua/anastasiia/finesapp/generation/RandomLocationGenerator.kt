package ua.anastasiia.finesapp.generation

import ua.anastasiia.finesapp.utils.generatePointsOnRoad

class RandomLocationGenerator : RandomFieldGenerator<Pair<Double, Double>> {

    override fun generate(number: Int): List<Pair<Double, Double>> {
        return generatePointsOnRoad(number)
    }
}
