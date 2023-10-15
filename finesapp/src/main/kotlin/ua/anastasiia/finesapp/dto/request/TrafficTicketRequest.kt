package ua.anastasiia.finesapp.dto.request

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import ua.anastasiia.finesapp.annotation.IntListValidator
import ua.anastasiia.finesapp.dto.toId
import ua.anastasiia.finesapp.dto.toViolation
import ua.anastasiia.finesapp.dto.toViolationType
import ua.anastasiia.finesapp.entity.MongoFine
import java.time.LocalDateTime

data class TrafficTicketRequest(
    val id: ObjectId? = null,
    val longitude: Double,
    val latitude: Double,
    val dateTime: LocalDateTime,
    val photoUrl: String,
    @Suppress("MagicNumber")
    @field:IntListValidator(0, 9)
    val violationIds: List<Int>
)

fun TrafficTicketRequest.toTrafficTicket() = MongoFine.TrafficTicket(
    id = id ?: ObjectId(),
    location = GeoJsonPoint(longitude, latitude),
    dateTime = dateTime,
    photoUrl = photoUrl,
    violations = violationIds.map { violationId ->
        violationId.toViolationType().toViolation()
    }
)

fun MongoFine.TrafficTicket.toRequest() = TrafficTicketRequest(
    id = id,
    longitude = location.x,
    latitude = location.y,
    dateTime = dateTime,
    photoUrl = photoUrl,
    violationIds = violations.map { violation ->
        violation.toId()
    }
)
