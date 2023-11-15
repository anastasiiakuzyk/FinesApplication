package ua.anastasiia.finesapp.domain

import java.time.LocalDateTime

data class Fine(
    val id: String? = null,
    val car: Car,
    val trafficTickets: List<TrafficTicket>
) {

    data class Car(
        val plate: String,
        val make: String,
        val model: String,
        val color: String
    )

    data class TrafficTicket(
        val id: String? = null,
        val locationLat: Double,
        val locationLon: Double,
        val dateTime: LocalDateTime,
        val photoUrl: String,
        val violations: List<Violation>
    ) {

        data class Violation(
            val description: String,
            val price: Double
        )
    }
}
