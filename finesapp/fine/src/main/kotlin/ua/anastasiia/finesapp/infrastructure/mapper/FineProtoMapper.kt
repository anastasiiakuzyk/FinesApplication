package ua.anastasiia.finesapp.infrastructure.mapper

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.commonmodels.fine.Fine as ProtoFine

fun Fine.toProto(): ProtoFine {
    val builder = ProtoFine.newBuilder()
        .setCar(car.toProto())
        .addAllTrafficTickets(trafficTickets.map { it.toProto() })
    id?.let {
        builder.setId(it)
    }
    return builder.build()
}

fun ProtoFine.toFine() = Fine(
    id = id.takeIf { this.hasId() }?.let { ObjectId(it).toHexString() },
    car = car.toCar(),
    trafficTickets = trafficTicketsList.map { it.toTrafficTicket() }
)
