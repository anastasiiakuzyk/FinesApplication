package ua.anastasiia.finesapp.controller.nats

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.springframework.stereotype.Component
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

    override fun handle(request: CreateFineRequest): CreateFineResponse = runCatching {
        val protoFine = saveProtoFine(request)
        publishEvent(protoFine, request.fine.car.plate)
        buildSuccessResponse(protoFine)
    }.getOrElse { exception ->
        buildFailureResponse(exception)
    }

    private fun saveProtoFine(request: CreateFineRequest): Fine {
        val fine = request.fine.toFine()
        return fineService.saveFine(fine.toRequest()).toFine().toProto()
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
                is CarPlateDuplicateException -> failureBuilder.apply {
                    carPlateDuplicateErrorBuilder.setMessage(exception.message)
                }

                else -> failureBuilder.setMessage(exception.message)
            }
        }.build()
}
