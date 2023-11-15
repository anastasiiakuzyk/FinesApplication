package ua.anastasiia.finesapp.infrastructure.dto.request

import ua.anastasiia.finesapp.infrastructure.annotation.IntListValidator
import java.time.LocalDateTime

data class TrafficTicketRequest(
    val id: String? = null,
    val longitude: Double,
    val latitude: Double,
    val dateTime: LocalDateTime,
    val photoUrl: String,
    @Suppress("MagicNumber")
    @field:IntListValidator(0, 9)
    val violationIds: List<Int>
)
