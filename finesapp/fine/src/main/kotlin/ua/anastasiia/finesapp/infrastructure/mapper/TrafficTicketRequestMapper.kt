package ua.anastasiia.finesapp.infrastructure.mapper

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.dto.request.TrafficTicketRequest

fun TrafficTicketRequest.toTrafficTicket() = Fine.TrafficTicket(
    id = id ?: ObjectId().toHexString(),
    locationLat = latitude,
    locationLon = longitude,
    dateTime = dateTime,
    photoUrl = photoUrl,
    violations = violationIds.map { violationId ->
        violationId.toViolationType().toViolation()
    }
)

fun Fine.TrafficTicket.toRequest() = TrafficTicketRequest(
    id = id,
    longitude = locationLon,
    latitude = locationLat,
    dateTime = dateTime,
    photoUrl = photoUrl,
    violationIds = violations.map { violation ->
        violation.toId()
    }
)
