package ua.anastasiia.finesapp.application.exception

open class TrafficTicketException(override val message: String) : RuntimeException()

class TrafficTicketNotFoundException(plate: String, id: String) :
    TrafficTicketException("Traffic ticket for car $plate with id: $id doesn't exist.")

class TrafficTicketWithViolationNotFoundException(tickedId: String, violationDescription: String) :
    TrafficTicketException(
        "Traffic ticket with id:$tickedId and violation: $violationDescription not found."
    )
