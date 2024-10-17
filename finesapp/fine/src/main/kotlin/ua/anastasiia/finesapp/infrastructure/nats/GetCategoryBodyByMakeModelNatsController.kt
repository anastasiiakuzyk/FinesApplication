package ua.anastasiia.finesapp.infrastructure.nats

import com.google.protobuf.Parser
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.exception.FineIdNotFoundException
import ua.anastasiia.finesapp.input.reqreply.fine.GetCategoryBodyByMakeModelRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetCategoryBodyByMakeModelResponse
import ua.anastasiia.finesapp.input.reqreply.fine.GetCategoryBodyByMakeModelResponse.Success.CategoryBody
import ua.anastasiia.finesapp.service.GetCategoryBodyByMakeModelService

@Component
class GetCategoryBodyByMakeModelNatsController(
    private val categoryBodyByMakeModelService: GetCategoryBodyByMakeModelService
) : NatsController<GetCategoryBodyByMakeModelRequest, GetCategoryBodyByMakeModelResponse> {
    override val subject: String = NatsSubject.MakeModel.GET_CATEGORY_BODY

    override val parser: Parser<GetCategoryBodyByMakeModelRequest> = GetCategoryBodyByMakeModelRequest.parser()
    override fun handle(request: GetCategoryBodyByMakeModelRequest): Mono<GetCategoryBodyByMakeModelResponse> =
        getCategoryAndBodyIdByMarkAndModel(request)
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun getCategoryAndBodyIdByMarkAndModel(request: GetCategoryBodyByMakeModelRequest): Mono<CategoryBody> {
        val result = categoryBodyByMakeModelService.getCategoryAndBodyIdByMarkAndModel(request.make, request.model)
        return CategoryBody.newBuilder()
            .setCategory(result.first)
            .setBody(result.second)
            .build().toMono()
    }


    private fun buildSuccessResponse(categoryBody: CategoryBody): GetCategoryBodyByMakeModelResponse =
        GetCategoryBodyByMakeModelResponse.newBuilder().apply { successBuilder.setCategoryBody(categoryBody) }.build()

    private fun buildFailureResponse(exception: Throwable): GetCategoryBodyByMakeModelResponse =
        GetCategoryBodyByMakeModelResponse.newBuilder().apply {
            when (exception) {
                is FineIdNotFoundException -> failureBuilder.fineIdNotFoundErrorBuilder
                else -> failureBuilder.setMessage(exception.message)
            }
        }.build()
}
