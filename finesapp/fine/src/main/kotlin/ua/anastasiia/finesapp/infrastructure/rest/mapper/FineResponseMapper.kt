package ua.anastasiia.finesapp.infrastructure.rest.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toCar
import ua.anastasiia.finesapp.infrastructure.mapper.toResponse
import ua.anastasiia.finesapp.infrastructure.rest.dto.response.FineResponse

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
