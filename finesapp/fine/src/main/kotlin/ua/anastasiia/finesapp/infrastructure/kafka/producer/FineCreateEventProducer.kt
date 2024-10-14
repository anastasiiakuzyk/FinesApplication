package ua.anastasiia.finesapp.infrastructure.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.KafkaTopic
import ua.anastasiia.finesapp.application.port.output.FineCreatedProducerOutPort
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.output.pubsub.fine.FineCreatedEvent

@Component
class FineCreateEventProducer(
    private val kafkaSender: KafkaSender<String, FineCreatedEvent>
) : FineCreatedProducerOutPort {

    override fun sendEvent(fine: Fine) {
        val event = FineCreatedEvent.newBuilder()
            .setFine(fine.toProto()).build()
        kafkaSender.send(
            SenderRecord.create(
                ProducerRecord(
                    KafkaTopic.CREATE_FINE,
                    event.fine.id,
                    event
                ),
                null
            )
                .toMono()
        ).subscribe()
    }
}
