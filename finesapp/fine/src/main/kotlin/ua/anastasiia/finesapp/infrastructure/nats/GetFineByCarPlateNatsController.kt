package ua.anastasiia.finesapp.infrastructure.nats

import com.google.protobuf.Parser
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.application.port.input.FineServiceIn
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateResponse

@Component
class GetFineByCarPlateNatsController(
    private val fineService: FineServiceIn
) : NatsController<GetFineByCarPlateRequest, GetFineByCarPlateResponse> {
    override val subject = NatsSubject.Fine.GET_BY_CAR_PLATE
    override val parser: Parser<GetFineByCarPlateRequest> = GetFineByCarPlateRequest.parser()

    override fun handle(request: GetFineByCarPlateRequest): Mono<GetFineByCarPlateResponse> =
        getProtoFineByCarPlate(request)
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun getProtoFineByCarPlate(request: GetFineByCarPlateRequest): Mono<Fine> =
        fineService.getFineByCarPlate(request.carPlate)
            .map { it.toProto() }

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
