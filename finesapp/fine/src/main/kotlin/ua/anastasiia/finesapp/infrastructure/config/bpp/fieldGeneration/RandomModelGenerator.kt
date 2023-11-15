package ua.anastasiia.finesapp.infrastructure.config.bpp.fieldGeneration

import ua.anastasiia.finesapp.infrastructure.config.bpp.fieldGeneration.web.CarMakeModel
import ua.anastasiia.finesapp.infrastructure.config.bpp.fieldGeneration.web.getAllCarMakeModels
import kotlin.random.Random

class RandomModelGenerator : RandomFieldGenerator {
    override fun generate(): String {
        val cars: List<CarMakeModel> =
            getAllCarMakeModels()
        return cars[Random.nextInt(Random.nextInt(cars.size))].model.capitalizeFirstLetter()
    }
}
