package ua.anastasiia.finesapp.infrastructure.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.dto.request.CarRequest

fun CarRequest.toCar() = Fine.Car(
    plate = plate,
    make = make,
    model = model!!,
    color = color
)

fun Fine.Car.toRequest() = CarRequest(
    plate = plate,
    make = make,
    model = model,
    color = color
)
