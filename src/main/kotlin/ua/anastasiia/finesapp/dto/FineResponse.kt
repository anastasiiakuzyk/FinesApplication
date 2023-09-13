package ua.anastasiia.finesapp.dto

import ua.anastasiia.finesapp.entity.Fine
import ua.anastasiia.finesapp.entity.Violation
import java.time.LocalDateTime

data class FineResponse(
    val id: Long,
    val longitude: Double,
    val latitude: Double,
    val dateTime: LocalDateTime,
    val photoUrl: String,
    val plate: String,
    val violations: MutableList<Violation>
)

fun Fine.toResponse() = FineResponse(
    id = id!!,
    longitude = longitude,
    latitude = latitude,
    dateTime = dateTime,
    photoUrl = photoUrl,
    plate = car!!.plate,
    violations = violations
)
