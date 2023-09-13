package ua.anastasiia.finesapp.exception

import java.time.LocalDateTime

data class ErrorMessage(
    val statusCode: Int,
    val timeStamp: LocalDateTime,
    val message: String,
    val description: String
)
