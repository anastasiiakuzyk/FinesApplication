package ua.anastasiia.finesapp.exception

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.dto.Violation

open class TrafficTicketException(override val message: String) : RuntimeException()

class TrafficTicketNotFoundException(plate: String, id: ObjectId) :
    TrafficTicketException("Traffic ticket for car $plate with id: $id doesn't exist.")

class TrafficTicketWithViolationNotFoundException(fineId: ObjectId, violationId: Int) :
    TrafficTicketException("Fine with id:$fineId and violation: ${Violation.entries[violationId]} not found.")
