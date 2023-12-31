package ua.anastasiia.finesapp.infrastructure.nats.event

import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.port.output.TrafficTicketAddedEventSubscriberOutPort
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

@Component
class NatsEventSubscriber(private val connection: Connection) : TrafficTicketAddedEventSubscriberOutPort {

    override fun subscribe(carPlate: String): Flux<TrafficTicketAddedEvent> {
        val sink = Sinks.many().multicast().onBackpressureBuffer<TrafficTicketAddedEvent>()
        connection.createDispatcher { message ->
            sink.tryEmitNext(TrafficTicketAddedEvent.parseFrom(message.data))
        }.subscribe(NatsSubject.TrafficTicket.addedSubject(carPlate))
        return sink.asFlux()
    }
}
