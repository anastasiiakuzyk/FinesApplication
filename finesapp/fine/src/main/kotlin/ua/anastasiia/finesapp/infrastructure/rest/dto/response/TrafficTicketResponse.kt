package ua.anastasiia.finesapp.infrastructure.rest.dto.response

data class TrafficTicketResponse(
    val id: String?,
    val longitude: Double,
    val latitude: Double,
    val dateTime: String,
    val photoUrl: String,
    val violations: List<ViolationResponse>,
    val valid: Boolean = true
)
