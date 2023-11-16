package ua.anastasiia.finesapp.infrastructure.rest.dto.response

data class CarResponse(
    val plate: String,
    val make: String,
    val model: String,
    val color: String
)
