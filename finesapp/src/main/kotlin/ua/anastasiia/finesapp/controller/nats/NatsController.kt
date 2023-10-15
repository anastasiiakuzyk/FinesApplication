package ua.anastasiia.finesapp.controller.nats

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser

interface NatsController<RequestT : GeneratedMessageV3, ResponseT : GeneratedMessageV3> {

    val subject: String

    val parser: Parser<RequestT>

    fun handle(request: RequestT): ResponseT
}
