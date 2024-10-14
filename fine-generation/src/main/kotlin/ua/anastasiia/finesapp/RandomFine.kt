package ua.anastasiia.finesapp

import ua.anastasiia.finesapp.generation.RandomColorGenerator
import ua.anastasiia.finesapp.generation.RandomDateTimeGenerator
import ua.anastasiia.finesapp.generation.RandomFieldGenerator
import ua.anastasiia.finesapp.generation.RandomImageUrlGenerator
import ua.anastasiia.finesapp.generation.RandomLocationGenerator
import ua.anastasiia.finesapp.generation.RandomMakeModelGenerator
import ua.anastasiia.finesapp.generation.RandomPlateGenerator
import ua.anastasiia.finesapp.generation.RandomViolationGenerator
import ua.anastasiia.finesapp.generation.domain.Fine

fun generateFines(number: Int): List<Fine> {
    val plates = generateRandomData(RandomPlateGenerator(), number)
    val makeModels = generateRandomData(RandomMakeModelGenerator(), number)
    val colors = generateRandomData(RandomColorGenerator(), number)
    val randomLocations = generateRandomData(RandomLocationGenerator(), number)
    val dates = generateRandomData(RandomDateTimeGenerator(), number)
    val images = generateRandomData(RandomImageUrlGenerator(), number)
    val violations = generateRandomData(RandomViolationGenerator(), number)

    return List(number) { i: Int ->
        Fine(
            car = Fine.Car(
                plate = plates[i],
                make = makeModels[i].first,
                model = makeModels[i].second,
                color = colors[i]
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
