package ua.anastasiia.finesapp.application.port.output

import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

interface TrafficTicketAddedEventConsumerOutPort {
    fun publish(trafficTicketAddedEvent: TrafficTicketAddedEvent)
}
