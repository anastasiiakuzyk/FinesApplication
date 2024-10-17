package ua.anastasiia.finesapp.service

import org.springframework.stereotype.Service
import ua.anastasiia.finesapp.utils.json.readCarDataFromJson

@Service
class GetCategoryBodyByMakeModelService {

    fun getCategoryAndBodyIdByMarkAndModel(
        markName: String,
        modelName: String
    ): Pair<String, String> {
        val car = readCarDataFromJson("cars_data_plus.json")
            .find { it.markName == markName && it.modelName == modelName }

        return car!!.autoData.let { mapCategoryIdToName(it!!.categoryId!!) to mapBodyIdToName(it.bodyId!!) }
    }

    private fun mapCategoryIdToName(id: Int): String {
        return when (id) {
            1 -> "passenger"
            2 -> "moto"
            3 -> "water"
            4 -> "special"
            5 -> "trailer"
            6 -> "truck"
            7 -> "bus"
            8 -> "caravan"
            9 -> "air"
            10 -> "agricultural"
            else -> "unknown"
        }
    }

    private fun mapBodyIdToName(id: Int): String {
        return when (id) {
            3 -> "sedan"
            5 -> "crossover"
            8 -> "minivan"
            449 -> "microvan"
            4 -> "hatchback"
            2 -> "universal"
            6 -> "coupe"
            7 -> "cabriolet"
            9 -> "pickup"
            307 -> "liftback"
            448 -> "fastback"
            252 -> "limousine"
            315 -> "roadster"
            else -> "unknown"
        }
    }
}
