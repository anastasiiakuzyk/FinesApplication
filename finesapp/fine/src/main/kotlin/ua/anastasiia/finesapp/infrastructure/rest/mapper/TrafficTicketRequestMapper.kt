package ua.anastasiia.finesapp.infrastructure.rest.mapper

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.rest.dto.request.TrafficTicketRequest
import ua.anastasiia.finesapp.infrastructure.mapper.toId
import ua.anastasiia.finesapp.infrastructure.mapper.toViolation
import ua.anastasiia.finesapp.infrastructure.mapper.toViolationType

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
