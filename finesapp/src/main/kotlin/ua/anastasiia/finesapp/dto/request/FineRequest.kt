package ua.anastasiia.finesapp.dto.request

import jakarta.validation.Valid
import org.bson.types.ObjectId
import ua.anastasiia.finesapp.entity.MongoFine

data class FineRequest(
    val id: String?,
    @field:Valid
    val car: CarRequest,
    @field:Valid
    val trafficTickets: List<TrafficTicketRequest>
)

fun FineRequest.toFine() = MongoFine(
    id = id?.let { ObjectId(id) },
    car = MongoFine.Car(
        plate = car.plate,
        make = car.make,
        model = car.model!!,
        color = car.color.uppercase()
    ),
    trafficTickets = trafficTickets.map { trafficTicketRequest ->
        trafficTicketRequest.toTrafficTicket()
    }
)

fun MongoFine.toRequest() = FineRequest(
    id = id?.toHexString(),
    car = car.toRequest(),
    trafficTickets = trafficTickets.map { trafficTicket ->
        trafficTicket.toRequest()
    }
)
