package ua.anastasiia.finesapp.infrastructure.kafka.consumer

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import ua.anastasiia.finesapp.infrastructure.nats.event.NatsEventPublisher
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

@Component
class FineKafkaReceiver(
    private val kafkaConsumer: KafkaReceiver<String, TrafficTicketAddedEvent>,
    private val natsEventPublisher: NatsEventPublisher
) {

    @PostConstruct
    fun initialize() {
        kafkaConsumer.receiveAutoAck()
            .flatMap { fluxRecord -> fluxRecord.map { natsEventPublisher.publish(it.value()) } }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }
}
