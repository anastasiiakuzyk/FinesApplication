package ua.anastasiia.finesapp.exception

import java.time.LocalDateTime

data class ValidationErrorMessage(
    val statusCode: Int,
    val timeStamp: LocalDateTime,
    val violations: List<ValidationError>,
    val description: String
) {
    data class ValidationError(
        val fieldName: String,
        val message: String
    )
}
