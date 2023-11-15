package ua.anastasiia.finesapp.infrastructure.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.dto.response.CarResponse

fun Fine.Car.toResponse() = CarResponse(
    plate = plate,
    make = make,
    model = model,
    color = color
)

fun CarResponse.toCar() = Fine.Car(
    plate = plate,
    make = make,
    model = model,
    color = color
)
