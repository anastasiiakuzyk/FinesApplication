package ua.anastasiia.finesapp.controller.nats

import com.google.protobuf.Parser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.dto.response.toFine
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.exception.FineIdNotFoundException
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdResponse
import ua.anastasiia.finesapp.service.FineService

@Component
class GetFineByIdNatsController(
    private val fineService: FineService
) : NatsController<GetFineByIdRequest, GetFineByIdResponse> {
    override val subject: String = NatsSubject.Fine.GET_BY_ID

    override val parser: Parser<GetFineByIdRequest> = GetFineByIdRequest.parser()
    override fun handle(request: GetFineByIdRequest): Mono<GetFineByIdResponse> =
        getProtoFineById(request)
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun getProtoFineById(request: GetFineByIdRequest): Mono<Fine> =
        fineService.getFineById(ObjectId(request.id))
            .map { it.toFine().toProto() }

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
