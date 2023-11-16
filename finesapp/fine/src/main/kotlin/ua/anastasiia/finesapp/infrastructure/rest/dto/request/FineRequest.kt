package ua.anastasiia.finesapp.infrastructure.rest.dto.request

import jakarta.validation.Valid

data class FineRequest(
    val id: String?,
    @field:Valid
    val car: CarRequest,
    @field:Valid
    val trafficTickets: List<TrafficTicketRequest>
)
