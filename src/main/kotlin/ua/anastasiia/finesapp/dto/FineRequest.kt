package ua.anastasiia.finesapp.dto

import java.time.LocalDateTime

data class FineRequest(
    val longitude: Double,
    val latitude: Double,
    val dateTime: LocalDateTime,
    val photoUrl: String,
    val plate: String
)
