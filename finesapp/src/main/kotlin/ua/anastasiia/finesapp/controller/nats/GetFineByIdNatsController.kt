package ua.anastasiia.finesapp.controller.nats

import com.google.protobuf.Parser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
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
    override fun handle(request: GetFineByIdRequest): GetFineByIdResponse = runCatching {
        val protoFine = getProtoFineById(request)
        buildSuccessResponse(protoFine)
    }.getOrElse { exception ->
        failureResponse(exception)
    }

    private fun getProtoFineById(request: GetFineByIdRequest): Fine =
        fineService.getFineById(ObjectId(request.id)).toFine().toProto()

    private fun buildSuccessResponse(fine: Fine): GetFineByIdResponse =
        GetFineByIdResponse.newBuilder().apply { successBuilder.setFine(fine) }.build()

    private fun failureResponse(exception: Throwable): GetFineByIdResponse =
        GetFineByIdResponse.newBuilder().apply {
            when (exception) {
                is FineIdNotFoundException -> failureBuilder.apply {
                    fineIdNotFoundErrorBuilder.setMessage(exception.message)
                }

                else -> failureBuilder.setMessage(exception.message)
            }
        }.build()
}
