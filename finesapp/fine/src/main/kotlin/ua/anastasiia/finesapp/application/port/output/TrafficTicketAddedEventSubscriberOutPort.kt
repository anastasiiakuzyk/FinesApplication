package ua.anastasiia.finesapp.application.port.output

import reactor.core.publisher.Flux
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

interface TrafficTicketAddedEventSubscriberOutPort {
    fun subscribe(carPlate: String): Flux<TrafficTicketAddedEvent>
}
