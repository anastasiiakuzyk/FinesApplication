package ua.anastasiia.finesapp.controller.nats

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.dto.request.toRequest
import ua.anastasiia.finesapp.dto.response.toFine
import ua.anastasiia.finesapp.dto.toFine
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.exception.CarPlateDuplicateException
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineRequest
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineResponse
import ua.anastasiia.finesapp.output.pubsub.fine.FineCreatedEvent
import ua.anastasiia.finesapp.service.FineService

@Component
class CreateFineNatsController(
    private val connection: Connection,
    private val fineService: FineService
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
        return fineService.saveFine(fine.toRequest())
            .map { it.toFine().toProto() }
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
