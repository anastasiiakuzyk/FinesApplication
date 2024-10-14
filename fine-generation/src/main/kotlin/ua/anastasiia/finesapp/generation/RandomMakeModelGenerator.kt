package ua.anastasiia.finesapp.generation

import ua.anastasiia.finesapp.utils.json.MakeModel
import ua.anastasiia.finesapp.utils.json.readMakeModelFromJson
import kotlin.random.Random

class RandomMakeModelGenerator : RandomFieldGenerator<Pair<String, String>> {

    override fun generate(number: Int): List<Pair<String, String>> {
        val makeModels: List<MakeModel> = readMakeModelFromJson("all-vehicles-model.json")
        return List(number) {
            val makeModel = makeModels[Random.nextInt(makeModels.size)]
            makeModel.make to makeModel.model
        }
    }
}
