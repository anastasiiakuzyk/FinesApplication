package ua.anastasiia.finesapp.infrastructure.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.dto.response.ViolationResponse

fun Fine.TrafficTicket.Violation.toResponse() = ViolationResponse(
    description = description,
    price = price
)

fun ViolationResponse.toViolation() = Fine.TrafficTicket.Violation(
    description = description,
    price = price
)
