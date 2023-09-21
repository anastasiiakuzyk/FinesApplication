package ua.anastasiia.finesapp.beanPostProcessor

import ua.anastasiia.finesapp.capitalizeFirstLetter
import ua.anastasiia.finesapp.web.CarMakeModel
import ua.anastasiia.finesapp.web.getAllCarMakeModels
import kotlin.random.Random

class RandomModelGenerator : RandomFieldGenerator {
    override fun generate(): String {
        val cars: List<CarMakeModel> =
            getAllCarMakeModels()
        return cars[Random.nextInt(Random.nextInt(cars.size))].model.capitalizeFirstLetter()
    }
}
