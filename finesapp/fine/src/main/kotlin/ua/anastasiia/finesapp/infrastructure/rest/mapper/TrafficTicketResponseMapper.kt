package ua.anastasiia.finesapp.infrastructure.rest.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.rest.dto.response.TrafficTicketResponse

fun Fine.TrafficTicket.toResponse() = TrafficTicketResponse(
    id = id,
    longitude = locationLon,
    latitude = locationLat,
    dateTime = dateTime.toString(),
    photoUrl = photoUrl,
    violations = violations.map { it.toResponse() },
    valid = valid
)

fun TrafficTicketResponse.toTrafficTicket() = Fine.TrafficTicket(
    id = id,
    locationLat = latitude,
    locationLon = longitude,
    dateTime = java.time.LocalDateTime.parse(dateTime),
    photoUrl = photoUrl,
    violations = violations.map { it.toViolation() },
    valid = valid
)
