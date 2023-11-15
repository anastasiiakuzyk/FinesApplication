package ua.anastasiia.finesapp.application.exception

import ua.anastasiia.finesapp.infrastructure.mapper.ViolationType

open class TrafficTicketException(override val message: String) : RuntimeException()

class TrafficTicketNotFoundException(plate: String, id: String) :
    TrafficTicketException("Traffic ticket for car $plate with id: $id doesn't exist.")

class TrafficTicketWithViolationNotFoundException(tickedId: String, violationId: Int) :
    TrafficTicketException(
        "Traffic ticket with id:$tickedId and violation: ${ViolationType.entries[violationId]} not found."
    )
