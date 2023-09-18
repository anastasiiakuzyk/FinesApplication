package ua.anastasiia.finesapp.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import ua.anastasiia.finesapp.annotation.EnumValidator
import ua.anastasiia.finesapp.entity.Car

data class CarRequest(
    @field:Pattern(regexp = "^(?=(.*[A-ZА-ЯІЇҐЄ]){2,})([A-ZА-ЯІЇҐЄ0-9]{3,8})\$")
    val plate: String?,
    @field:Size(min = 1, max = 50)
    val mark: String,
    @field:Size(min = 1, max = 50)
    val model: String,
    @field:EnumValidator(Car.CarColor::class)
    val color: String
)

fun CarRequest.toEntity() = Car(
    plate = plate!!,
    mark = mark,
    model = model,
    color = color
)
