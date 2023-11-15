package ua.anastasiia.finesapp.infrastructure.nats

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.exception.CarPlateDuplicateException
import ua.anastasiia.finesapp.application.port.input.FineServiceIn
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toFine
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toFine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineRequest
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineResponse
import ua.anastasiia.finesapp.output.pubsub.fine.FineCreatedEvent

@Component
class CreateFineNatsController(
    private val connection: Connection,
    private val fineService: FineServiceIn
) : NatsController<CreateFineRequest, CreateFineResponse> {

    override val subject = NatsSubject.Fine.CREATE
    override val parser: Parser<CreateFineRequest> = CreateFineRequest.parser()

    override fun handle(request: CreateFineRequest): Mono<CreateFineResponse> =
        saveProtoFine(request)
            .doOnNext { protoFine -> publishEvent(protoFine, request.fine.car.plate) }
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun saveProtoFine(request: CreateFineRequest): Mono<Fine> {
        val fine = request.fine.toFine()
        return fineService.saveFine(fine)
            .map { it.toProto() }
    }

    private fun publishEvent(protoFine: Fine, carPlate: String) {
        val eventMessage = FineCreatedEvent.newBuilder().setFine(protoFine).build()
        val eventSubject = NatsSubject.Fine.createdSubject(carPlate)
        connection.publish(
            eventSubject,
            eventMessage.toByteArray()
        )
    }

    private fun buildSuccessResponse(protoFine: Fine): CreateFineResponse =
        CreateFineResponse.newBuilder().apply {
            successBuilder.setFine(protoFine)
        }.build()

    private fun buildFailureResponse(exception: Throwable): CreateFineResponse =
        CreateFineResponse.newBuilder().apply {
            when (exception) {
                is CarPlateDuplicateException -> failureBuilder.carPlateDuplicateErrorBuilder
                else -> failureBuilder.setMessage(exception.message)
            }
        }.build()
}
