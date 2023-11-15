package ua.anastasiia.finesapp.infrastructure.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.dto.response.FineResponse

fun Fine.toResponse() = FineResponse(
    id = id,
    car = car.toResponse(),
    trafficTickets = trafficTickets.map { it.toResponse() }
)

fun FineResponse.toFine() = Fine(
    id = id,
    car = car.toCar(),
    trafficTickets = trafficTickets.map { it.toTrafficTicket() }
)
