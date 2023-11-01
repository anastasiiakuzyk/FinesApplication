package ua.anastasiia.finesapp.beanPostProcessor

import com.google.protobuf.GeneratedMessageV3
import io.nats.client.Connection
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
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
        connection.createDispatcher { message ->
            val parsedData = controller.parser.parseFrom(message.data)
            controller.handle(parsedData)
                .map { it.toByteArray() }
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe { connection.publish(message.replyTo, it) }
        }.apply { subscribe(controller.subject) }
    }
}
