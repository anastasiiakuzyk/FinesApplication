package ua.anastasiia.finesapp.infrastructure.adapters.nats.event

import io.nats.client.Connection
import org.springframework.stereotype.Component
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

@Component
class NatsEventPublisher(private val connection: Connection) {

    fun publish(trafficTicketAddedEvent: TrafficTicketAddedEvent) {
        val updateEventSubject = NatsSubject.TrafficTicket.addedSubject(trafficTicketAddedEvent.fine.car.plate)
        connection.publish(updateEventSubject, trafficTicketAddedEvent.toByteArray())
    }
}
