package ua.anastasiia.finesapp.dto

import ua.anastasiia.finesapp.entity.Car

data class CarResponse(
    val id: Long,
    val plate: String,
    val mark: String,
    val model: String,
    val color: String
)

fun Car.toResponse() = CarResponse(
    id = id!!,
    plate = plate,
    mark = mark,
    model = model,
    color = color
)

fun CarResponse.toEntity() = Car(
    id = id,
    plate = plate,
    mark = mark,
    model = model,
    color = color
)
