package ua.anastasiia.finesapp.controller.nats

import com.google.protobuf.Parser
import org.springframework.stereotype.Component
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.commonmodels.car.Car
import ua.anastasiia.finesapp.dto.response.toCar
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.exception.CarsNotFoundException
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsRequest
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsResponse
import ua.anastasiia.finesapp.service.FineService

@Component
class GetAllCarsNatsController(
    private val fineService: FineService
) : NatsController<GetAllCarsRequest, GetAllCarsResponse> {

    override val subject: String = NatsSubject.Car.GET_ALL
    override val parser: Parser<GetAllCarsRequest> = GetAllCarsRequest.parser()
    override fun handle(request: GetAllCarsRequest): GetAllCarsResponse = runCatching {
        val protoCars = getAllProtoCars()
        buildSuccessResponse(protoCars)
    }.getOrElse { exception ->
        buildFailureResponse(exception)
    }

    private fun getAllProtoCars(): List<Car> = fineService.getAllCars().map { it.toCar().toProto() }

    private fun buildSuccessResponse(protoCars: List<Car>): GetAllCarsResponse =
        GetAllCarsResponse.newBuilder().apply {
            successBuilder.addAllCars(protoCars)
        }.build()

    private fun buildFailureResponse(exception: Throwable): GetAllCarsResponse =
        GetAllCarsResponse.newBuilder().apply {
            when (exception) {
                is CarsNotFoundException -> failureBuilder.carsNotFoundErrorBuilder
                else -> failureBuilder.setMessage(exception.stackTraceToString())
            }
        }.build()
}
