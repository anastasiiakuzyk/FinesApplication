package ua.anastasiia.finesapp.beanPostProcessor

import com.google.protobuf.GeneratedMessageV3
import io.nats.client.Connection
import io.nats.client.Message
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import ua.anastasiia.finesapp.controller.nats.NatsController

@Component
class InitNatsControllerBeanPostProcessor(val connection: Connection) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        if (bean is NatsController<*, *>) {
            initNatsController(bean)
        }
        return bean
    }

    private fun <RequestT : GeneratedMessageV3, ResponseT : GeneratedMessageV3> initNatsController(
        controller: NatsController<RequestT, ResponseT>
    ) {
        connection.createDispatcher { message: Message ->
            val request = controller.parser.parseFrom(message.data)
            val response = controller.handle(request)
            connection.publish(message.replyTo, response.toByteArray())
        }.subscribe(controller.subject)
    }
}
