package ua.anastasiia.finesapp.infrastructure.nats

import com.google.protobuf.Parser
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.exception.FineIdNotFoundException
import ua.anastasiia.finesapp.application.port.input.FineServiceIn
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdResponse

@Component
class GetFineByIdNatsController(
    private val fineService: FineServiceIn
) : NatsController<GetFineByIdRequest, GetFineByIdResponse> {
    override val subject: String = NatsSubject.Fine.GET_BY_ID

    override val parser: Parser<GetFineByIdRequest> = GetFineByIdRequest.parser()
    override fun handle(request: GetFineByIdRequest): Mono<GetFineByIdResponse> =
        getProtoFineById(request)
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun getProtoFineById(request: GetFineByIdRequest): Mono<Fine> =
        fineService.getFineById(request.id)
            .map { it.toProto() }

    private fun buildSuccessResponse(fine: Fine): GetFineByIdResponse =
        GetFineByIdResponse.newBuilder().apply { successBuilder.setFine(fine) }.build()

    private fun buildFailureResponse(exception: Throwable): GetFineByIdResponse =
        GetFineByIdResponse.newBuilder().apply {
            when (exception) {
                is FineIdNotFoundException -> failureBuilder.fineIdNotFoundErrorBuilder
                else -> failureBuilder.setMessage(exception.message)
            }
        }.build()
}
