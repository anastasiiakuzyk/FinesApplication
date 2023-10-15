package ua.anastasiia.finesapp.dto.response

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import ua.anastasiia.finesapp.entity.MongoFine

data class TrafficTicketResponse(
    val id: String?,
    val longitude: Double,
    val latitude: Double,
    val dateTime: String,
    val photoUrl: String,
    val violations: List<ViolationResponse>
)

fun MongoFine.TrafficTicket.toResponse() = TrafficTicketResponse(
    id = id.toHexString(),
    longitude = location.x,
    latitude = location.y,
    dateTime = dateTime.toString(),
    photoUrl = photoUrl,
    violations = violations.map { it.toResponse() }
)

fun TrafficTicketResponse.toTrafficTicket() = MongoFine.TrafficTicket(
    id = ObjectId(id),
    location = GeoJsonPoint(longitude, latitude),
    dateTime = java.time.LocalDateTime.parse(dateTime),
    photoUrl = photoUrl,
    violations = violations.map { it.toViolation() }
)
