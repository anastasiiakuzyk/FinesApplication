package ua.anastasiia.finesapp.config

import com.google.protobuf.GeneratedMessageV3
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import ua.anastasiia.finesapp.KafkaTopic
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String
) {

    @Bean
    fun kafkaSender(): KafkaSender<String, TrafficTicketAddedEvent> =
        KafkaSender.create(SenderOptions.create(producerProperties()))

    private fun producerProperties(): Map<String, Any> = mutableMapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaProtobufSerializer::class.java,
        SCHEMA_REGISTRY_URL_KEY to schemaRegistryUrl
    )

    @Bean
    fun kafkaReceiverTrafficTicketAddedEvent(): KafkaReceiver<String, TrafficTicketAddedEvent> {
        val customProperties: MutableMap<String, Any> = mutableMapOf(
            KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE to TrafficTicketAddedEvent::class.java.name
        )
        return createKafkaReceiver(consumerProperties(customProperties))
    }

    private fun <T : GeneratedMessageV3> createKafkaReceiver(
        properties: MutableMap<String, Any>,
    ): KafkaReceiver<String, T> {
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "fine-group"
        val options =
            ReceiverOptions.create<String, T>(properties).subscription(setOf(KafkaTopic.ADD_TRAFFIC_TICKET))
        return KafkaReceiver.create(options)
    }

    private fun consumerProperties(
        customProperties: MutableMap<String, Any> = mutableMapOf()
    ): MutableMap<String, Any> {
        val baseProperties: MutableMap<String, Any> = mutableMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaProtobufDeserializer::class.java.name,
            SCHEMA_REGISTRY_URL_KEY to schemaRegistryUrl,
            ConsumerConfig.GROUP_ID_CONFIG to "fine-group"
        )
        baseProperties.putAll(customProperties)
        return baseProperties
    }

    companion object {
        private const val SCHEMA_REGISTRY_URL_KEY = "schema.registry.url"
    }
}
