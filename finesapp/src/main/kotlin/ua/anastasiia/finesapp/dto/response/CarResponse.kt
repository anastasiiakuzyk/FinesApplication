package ua.anastasiia.finesapp.dto.response

import ua.anastasiia.finesapp.entity.MongoFine

data class CarResponse(
    val plate: String,
    val make: String,
    val model: String,
    val color: String
)

fun MongoFine.Car.toResponse() = CarResponse(
    plate = plate,
    make = make,
    model = model,
    color = color
)

fun CarResponse.toCar() = MongoFine.Car(
    plate = plate,
    make = make,
    model = model,
    color = color
)
