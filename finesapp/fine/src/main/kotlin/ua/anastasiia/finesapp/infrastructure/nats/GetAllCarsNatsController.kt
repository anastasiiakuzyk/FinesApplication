package ua.anastasiia.finesapp.infrastructure.nats

import com.google.protobuf.Parser
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.exception.CarsNotFoundException
import ua.anastasiia.finesapp.application.port.input.FineServiceInPort
import ua.anastasiia.finesapp.commonmodels.car.Car
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsRequest
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsResponse

@Component
class GetAllCarsNatsController(
    private val fineService: FineServiceInPort
) : NatsController<GetAllCarsRequest, GetAllCarsResponse> {

    override val subject: String = NatsSubject.Car.GET_ALL
    override val parser: Parser<GetAllCarsRequest> = GetAllCarsRequest.parser()
    override fun handle(request: GetAllCarsRequest): Mono<GetAllCarsResponse> =
        getAllProtoCars()
            .collectList()
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }

    private fun getAllProtoCars(): Flux<Car> = fineService.getAllCars()
        .map { it.toProto() }

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
