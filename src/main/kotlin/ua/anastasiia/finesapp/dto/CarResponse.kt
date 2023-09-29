package ua.anastasiia.finesapp.dto

import org.springframework.data.mongodb.core.mapping.Field

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
