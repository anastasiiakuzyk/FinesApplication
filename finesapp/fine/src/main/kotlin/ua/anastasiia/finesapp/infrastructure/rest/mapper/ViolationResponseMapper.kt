package ua.anastasiia.finesapp.infrastructure.rest.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.rest.dto.response.ViolationResponse

fun Fine.TrafficTicket.Violation.toResponse() = ViolationResponse(
    description = description,
    price = price
)

fun ViolationResponse.toViolation() = Fine.TrafficTicket.Violation(
    description = description,
    price = price
)
