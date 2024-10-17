package ua.anastasiia.finesapp.generation

import ua.anastasiia.finesapp.utils.json.CarData
import ua.anastasiia.finesapp.utils.json.readCarDataFromJson

class RandomCarDataGenerator : RandomFieldGenerator<CarData> {

    override fun generate(number: Int): List<CarData> {
        return readCarDataFromJson("cars_data_plus.json")
            .take(number)
    }
}
