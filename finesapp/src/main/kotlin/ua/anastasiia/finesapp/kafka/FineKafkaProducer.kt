package ua.anastasiia.finesapp.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.KafkaTopic
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

@Component
class FineKafkaProducer(
    private val kafkaSender: KafkaSender<String, TrafficTicketAddedEvent>
) {
    fun produceNotification(protoFine: Fine, trafficTicketId: String) {
        val event = TrafficTicketAddedEvent.newBuilder().setId(trafficTicketId).setFine(protoFine).build()
        kafkaSender.send(
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
