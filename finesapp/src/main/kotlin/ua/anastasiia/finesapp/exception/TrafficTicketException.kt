package ua.anastasiia.finesapp.exception

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.dto.ViolationType

open class TrafficTicketException(override val message: String) : RuntimeException()

class TrafficTicketNotFoundException(plate: String, id: ObjectId) :
    TrafficTicketException("Traffic ticket for car $plate with id: $id doesn't exist.")

class TrafficTicketWithViolationNotFoundException(tickedId: ObjectId, violationId: Int) :
    TrafficTicketException(
        "Traffic ticket with id:$tickedId and violation: ${ViolationType.entries[violationId]} not found."
    )
