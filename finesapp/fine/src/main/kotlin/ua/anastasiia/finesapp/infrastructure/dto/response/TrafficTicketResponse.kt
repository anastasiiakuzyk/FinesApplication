package ua.anastasiia.finesapp.infrastructure.dto.response

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toResponse
import ua.anastasiia.finesapp.infrastructure.mapper.toViolation

data class TrafficTicketResponse(
    val id: String?,
    val longitude: Double,
    val latitude: Double,
    val dateTime: String,
    val photoUrl: String,
    val violations: List<ViolationResponse>
)

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
    locationLon = longitude,
    locationLat = latitude,
    dateTime = java.time.LocalDateTime.parse(dateTime),
    photoUrl = photoUrl,
    violations = violations.map { it.toViolation() }
)
