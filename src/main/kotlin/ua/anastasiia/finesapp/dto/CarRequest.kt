package ua.anastasiia.finesapp.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import ua.anastasiia.finesapp.entity.Car

data class CarRequest(
    @field:Pattern(regexp = "^(?=(.*[A-ZА-ЯІЇҐЄ]){2,})([A-ZА-ЯІЇҐЄ0-9]{3,8})\$")
    val plate: String,
    @field:Size(min = 1, max = 50)
    val mark: String,
    @field:Size(min = 1, max = 50)
    val model: String,
    val color: Car.CarColor
)

fun CarRequest.toEntity() = Car(
    plate = plate,
    mark = mark,
    model = model,
    color = color
)
