package ua.anastasiia.finesapp.dto.response

import org.springframework.data.mongodb.core.mapping.Field
import ua.anastasiia.finesapp.entity.MongoFine

data class CarResponse(
    @Field("car.plate")
    val plate: String,
    @Field("car.make")
    val make: String,
    @Field("car.model")
    val model: String,
    @Field("car.color")
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
