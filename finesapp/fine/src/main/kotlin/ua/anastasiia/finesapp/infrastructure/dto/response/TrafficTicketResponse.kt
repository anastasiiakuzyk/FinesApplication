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
