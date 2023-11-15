package ua.anastasiia.finesapp.infrastructure.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.dto.response.TrafficTicketResponse

fun Fine.TrafficTicket.toResponse() = TrafficTicketResponse(
    id = id,
    longitude = locationLon,
    latitude = locationLat,
    dateTime = dateTime.toString(),
    photoUrl = photoUrl,
    violations = violations.map { it.toResponse() }
)

fun TrafficTicketResponse.toTrafficTicket() = Fine.TrafficTicket(
    id = id,
    locationLat = latitude,
    locationLon = longitude,
    dateTime = java.time.LocalDateTime.parse(dateTime),
    photoUrl = photoUrl,
    violations = violations.map { it.toViolation() }
)
