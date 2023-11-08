package ua.anastasiia.finesapp.controller.nats

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.dto.response.toFine
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.exception.TrafficTicketWithViolationNotFoundException
import ua.anastasiia.finesapp.input.reqreply.violation.DeleteViolationRequest
import ua.anastasiia.finesapp.input.reqreply.violation.DeleteViolationResponse
import ua.anastasiia.finesapp.output.pubsub.violation.ViolationDeletedEvent
import ua.anastasiia.finesapp.service.FineService

@Component
class DeleteViolationNatsController(
    private val connection: Connection,
    private val fineService: FineService
) : NatsController<DeleteViolationRequest, DeleteViolationResponse> {

    override val subject = NatsSubject.Violation.DELETE
    override val parser: Parser<DeleteViolationRequest> = DeleteViolationRequest.parser()

    override fun handle(request: DeleteViolationRequest): Mono<DeleteViolationResponse> =
        removeViolationFromTicket(request)
            .doOnNext { protoFine -> publishEvent(protoFine, request.carPlate) }
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun removeViolationFromTicket(request: DeleteViolationRequest): Mono<Fine> {
        val carPlate = request.carPlate
        val ticketId = request.ticketId
        val violationId = request.violationId
        return fineService.removeViolationFromTicket(carPlate, ObjectId(ticketId), violationId)
            .map { it.toFine().toProto() }
    }

    private fun publishEvent(protoFine: Fine, carPlate: String) {
        val eventMessage = ViolationDeletedEvent.newBuilder().setFine(protoFine).build()
        val eventSubject = NatsSubject.Violation.deletedSubject(carPlate)
        connection.publish(
            eventSubject,
            eventMessage.toByteArray()
        )
    }

    private fun buildSuccessResponse(protoFine: Fine): DeleteViolationResponse =
        DeleteViolationResponse.newBuilder().apply {
            successBuilder.setFine(protoFine)
        }.build()

    private fun buildFailureResponse(exception: Throwable): DeleteViolationResponse =
        DeleteViolationResponse.newBuilder().apply {
            when (exception) {
                is TrafficTicketWithViolationNotFoundException ->
                    failureBuilder.trafficTicketWithViolationNotFoundErrorBuilder

                else -> failureBuilder.setMessage(exception.stackTraceToString())
            }
        }.build()
}
