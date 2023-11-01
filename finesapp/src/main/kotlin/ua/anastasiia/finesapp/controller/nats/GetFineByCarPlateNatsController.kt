package ua.anastasiia.finesapp.controller.nats

import com.google.protobuf.Parser
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.dto.response.toFine
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateResponse
import ua.anastasiia.finesapp.service.FineService

@Component
class GetFineByCarPlateNatsController(
    private val fineService: FineService
) : NatsController<GetFineByCarPlateRequest, GetFineByCarPlateResponse> {
    override val subject = NatsSubject.Fine.GET_BY_CAR_PLATE
    override val parser: Parser<GetFineByCarPlateRequest> = GetFineByCarPlateRequest.parser()

    override fun handle(request: GetFineByCarPlateRequest): Mono<GetFineByCarPlateResponse> =
        getProtoFineByCarPlate(request)
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun getProtoFineByCarPlate(request: GetFineByCarPlateRequest): Mono<Fine> =
        fineService.getFineByCarPlate(request.carPlate)
            .map { it.toFine().toProto() }

    private fun buildSuccessResponse(fine: Fine): GetFineByCarPlateResponse =
        GetFineByCarPlateResponse.newBuilder().apply { successBuilder.setFine(fine) }.build()

    private fun buildFailureResponse(exception: Throwable): GetFineByCarPlateResponse =
        GetFineByCarPlateResponse.newBuilder().apply {
            when (exception) {
                is CarPlateNotFoundException -> failureBuilder.carPlateNotFoundErrorBuilder
                else -> failureBuilder.setMessage(exception.message)
            }
        }.build()
}
