package ua.anastasiia.finesapp.infrastructure.rest.mapper

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.rest.dto.request.FineRequest

fun FineRequest.toFine() = Fine(
    id = id?.let { ObjectId(id).toHexString() },
    car = Fine.Car(
        plate = car.plate,
        make = car.make,
        model = car.model!!,
        color = car.color.uppercase()
    ),
    trafficTickets = trafficTickets.map { trafficTicketRequest ->
        trafficTicketRequest.toTrafficTicket()
    }
)

fun Fine.toRequest() = FineRequest(
    id = id,
    car = car.toRequest(),
    trafficTickets = trafficTickets.map { trafficTicket ->
        trafficTicket.toRequest()
    }
)
