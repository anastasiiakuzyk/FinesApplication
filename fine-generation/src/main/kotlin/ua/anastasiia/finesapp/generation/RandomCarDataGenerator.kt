package ua.anastasiia.finesapp.generation

import ua.anastasiia.finesapp.utils.json.CarData
import ua.anastasiia.finesapp.utils.json.readCarDataFromJson
import kotlin.random.Random

class RandomCarDataGenerator : RandomFieldGenerator<CarData> {

    override fun generate(number: Int): List<CarData> {
        val carData: List<CarData> = readCarDataFromJson("cars_data_plus.json")
            .asSequence()
            .filter { it.plateNumber != null }
            .filter { it.markName != null }
            .filter { it.modelName != null }
            .filter { it.color.eng != null }
            .filter { it.autoData.categoryId != null }
            .filter { it.autoData.bodyId != null }
            .toList()
        return List(number) { carData[Random.nextInt(carData.size)] }
    }
}
