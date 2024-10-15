package ua.anastasiia.finesapp.generation

import ua.anastasiia.finesapp.utils.json.CarData
import ua.anastasiia.finesapp.utils.json.readCarDataFromJson
import kotlin.random.Random

class RandomImageUrlGenerator : RandomFieldGenerator<String> {

    override fun generate(number: Int): List<String> {
        val carData: MutableList<CarData> = readCarDataFromJson("cars_data_plus.json")
        return List(number) { carData[Random.nextInt(carData.size)].photoData.seoLinkF }
    }
}
