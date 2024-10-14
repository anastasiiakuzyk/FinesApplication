package ua.anastasiia.finesapp.infrastructure.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.KafkaTopic
import ua.anastasiia.finesapp.application.port.output.TrafficTicketAddedEventProducerOutPort
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

@Component
class TrafficTicketAddedEventProducer(
    private val kafkaTrafficTicketAddedSender: KafkaSender<String, TrafficTicketAddedEvent>
) : TrafficTicketAddedEventProducerOutPort {
    override fun sendEvent(fine: Fine, trafficTicketId: Fine.TrafficTicket) {
        val event = TrafficTicketAddedEvent.newBuilder()
            .setId(trafficTicketId.toProto().id)
            .setFine(fine.toProto()).build()
        kafkaTrafficTicketAddedSender.send(
            SenderRecord.create(
                ProducerRecord(
                    KafkaTopic.ADD_TRAFFIC_TICKET,
                    event.id,
                    event
                ),
                null
            )
                .toMono()
        ).subscribe()
    }
}
