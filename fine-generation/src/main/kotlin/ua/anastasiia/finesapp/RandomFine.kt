package ua.anastasiia.finesapp

import ua.anastasiia.finesapp.entity.adaptColor
import ua.anastasiia.finesapp.generation.RandomCarDataGenerator
import ua.anastasiia.finesapp.generation.RandomDateTimeGenerator
import ua.anastasiia.finesapp.generation.RandomFieldGenerator
import ua.anastasiia.finesapp.generation.RandomLocationGenerator
import ua.anastasiia.finesapp.generation.RandomViolationGenerator
import ua.anastasiia.finesapp.generation.domain.Fine
import ua.anastasiia.finesapp.utils.json.CarData

fun generateFines(number: Int): List<Fine> {
    val carData: List<CarData> = generateRandomData(RandomCarDataGenerator(), number)

    val plates = carData.map { it.plateNumber!!.replace(" ", "") }
    val makes = carData.map { it.markName!! }
    val models = carData.map { it.modelName!! }
    val colors = carData.map { adaptColor(it.color.eng!!) }
    val images = carData.map { it.photoData.seoLinkF }

    val randomLocations = generateRandomData(RandomLocationGenerator(), number)
    val dates = generateRandomData(RandomDateTimeGenerator(), number)
    val violations = generateRandomData(RandomViolationGenerator(), number)

    return List(number) { i: Int ->
        Fine(
            car = Fine.Car(
                plate = plates[i],
                make = makes[i],
                model = models[i],
                color = colors[i].name
            ),
            trafficTickets = listOf(
                Fine.TrafficTicket(
                    locationLat = randomLocations[i].first,
                    locationLon = randomLocations[i].second,
                    dateTime = dates[i],
                    photoUrl = images[i],
                    violations = violations[i]
                )
            )
        )
    }
}

private fun <T> generateRandomData(generator: RandomFieldGenerator<T>, number: Int): List<T> {
    return generator.generate(number)
}
