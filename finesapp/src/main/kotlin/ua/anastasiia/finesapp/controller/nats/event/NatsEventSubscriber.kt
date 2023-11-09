package ua.anastasiia.finesapp.controller.nats.event

import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

@Component
class NatsEventSubscriber(private val connection: Connection) {

    fun subscribe(carPlate: String): Flux<TrafficTicketAddedEvent> {
        Sinks.many().multicast().onBackpressureBuffer<TrafficTicketAddedEvent>().apply {
            connection.createDispatcher { message ->
                tryEmitNext(TrafficTicketAddedEvent.parseFrom(message.data))
            }.subscribe(NatsSubject.TrafficTicket.addedSubject(carPlate))
            return asFlux()
        }
    }
}
