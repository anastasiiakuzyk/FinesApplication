package ua.anastasiia.finesapp.dto.response

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.entity.MongoFine

data class FineResponse(
    val id: String?,
    val car: CarResponse,
    val trafficTickets: List<TrafficTicketResponse>
)

fun MongoFine.toResponse() = FineResponse(
    id = id?.toHexString(),
    car = car.toResponse(),
    trafficTickets = trafficTickets.map { it.toResponse() }
)

fun FineResponse.toFine() = MongoFine(
    id = ObjectId(id),
    car = car.toCar(),
    trafficTickets = trafficTickets.map { it.toTrafficTicket() }
)
