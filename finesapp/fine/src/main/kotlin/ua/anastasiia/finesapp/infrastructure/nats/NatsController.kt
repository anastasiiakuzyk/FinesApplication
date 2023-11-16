package ua.anastasiia.finesapp.infrastructure.nats

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import reactor.core.publisher.Mono

interface NatsController<RequestT : GeneratedMessageV3, ResponseT : GeneratedMessageV3> {

    val subject: String

    val parser: Parser<RequestT>

    fun handle(request: RequestT): Mono<ResponseT>
}
