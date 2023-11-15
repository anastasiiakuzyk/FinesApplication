package ua.anastasiia.finesapp.infrastructure.nats

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.application.port.input.FineServiceIn
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.infrastructure.mapper.toTrafficTicket
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toTrafficTicket
import ua.anastasiia.finesapp.input.reqreply.trafficticket.AddTrafficTicketRequest
import ua.anastasiia.finesapp.input.reqreply.trafficticket.AddTrafficTicketResponse
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent

@Component
class AddTrafficTicketNatsController(
    private val connection: Connection,
    private val fineService: FineServiceIn
) : NatsController<AddTrafficTicketRequest, AddTrafficTicketResponse> {

    override val subject = NatsSubject.TrafficTicket.ADD
    override val parser: Parser<AddTrafficTicketRequest> = AddTrafficTicketRequest.parser()

    override fun handle(request: AddTrafficTicketRequest): Mono<AddTrafficTicketResponse> =
        addProtoTrafficTicketByCarPlate(request)
            .doOnNext { protoFine -> publishEvent(protoFine, request.carPlate) }
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun addProtoTrafficTicketByCarPlate(request: AddTrafficTicketRequest): Mono<Fine> {
        val carPlate = request.carPlate
        val trafficTicket = request.trafficTicket.toTrafficTicket()
        return fineService.addTrafficTicketByCarPlate(carPlate, trafficTicket)
            .map { it.toProto() }
    }

    private fun publishEvent(protoFine: Fine, carPlate: String) {
        val eventMessage = TrafficTicketAddedEvent.newBuilder().setFine(protoFine).build()
        val eventSubject = NatsSubject.TrafficTicket.addedSubject(carPlate)
        connection.publish(
            eventSubject,
            eventMessage.toByteArray()
        )
    }

    private fun buildSuccessResponse(protoFine: Fine): AddTrafficTicketResponse =
        AddTrafficTicketResponse.newBuilder().apply {
            successBuilder.setFine(protoFine)
        }.build()

    private fun buildFailureResponse(exception: Throwable): AddTrafficTicketResponse =
        AddTrafficTicketResponse.newBuilder().apply {
            when (exception) {
                is CarPlateNotFoundException -> failureBuilder.carPlateNotFoundBuilder
                else -> failureBuilder.setMessage(exception.stackTraceToString())
            }
        }.build()
}
