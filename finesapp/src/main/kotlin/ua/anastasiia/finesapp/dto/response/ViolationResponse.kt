package ua.anastasiia.finesapp.dto.response

import ua.anastasiia.finesapp.entity.MongoFine

data class ViolationResponse(
    val description: String,
    val price: Double
)

fun MongoFine.TrafficTicket.Violation.toResponse() = ViolationResponse(
    description = description,
    price = price
)

fun ViolationResponse.toViolation() = MongoFine.TrafficTicket.Violation(
    description = description,
    price = price
)
