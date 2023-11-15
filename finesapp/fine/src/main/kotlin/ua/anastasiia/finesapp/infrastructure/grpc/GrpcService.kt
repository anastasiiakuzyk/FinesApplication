package ua.anastasiia.finesapp.infrastructure.grpc

import net.devh.boot.grpc.server.service.GrpcService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.ReactorFinesServiceGrpc
import ua.anastasiia.finesapp.application.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.application.exception.CarsNotFoundException
import ua.anastasiia.finesapp.application.port.input.FineServiceIn
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toFine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.infrastructure.nats.event.NatsEventSubscriber
import ua.anastasiia.finesapp.infrastructure.rest.dto.response.CarResponse
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toCar
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toResponse
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsRequest
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsResponse
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateResponse
import ua.anastasiia.finesapp.input.reqreply.fine.StreamByCarPlateRequest
import ua.anastasiia.finesapp.input.reqreply.fine.StreamByCarPlateResponse

@GrpcService
class GrpcService(
    private val fineService: FineServiceIn,
    private val natsEventSubscriber: NatsEventSubscriber
) : ReactorFinesServiceGrpc.FinesServiceImplBase() {

    override fun getAllCars(request: Mono<GetAllCarsRequest>): Mono<GetAllCarsResponse> {
        return request
            .flatMap { fineService.getAllCars().collectList() }
            .map { cars -> buildAllCarsResponse(cars.map { it.toResponse() }) }
            .onErrorResume { exception -> handleCarsException(exception) }
    }

    override fun getByCarPlate(request: Mono<GetFineByCarPlateRequest>): Mono<GetFineByCarPlateResponse> {
        return request
            .flatMap { fineService.getFineByCarPlate(it.carPlate) }
            .map { fine -> buildGetFineByCarPlateResponse(fine) }
            .onErrorResume { exception -> handleCarPlateException(exception) }
    }

    override fun streamByCarPlate(request: Mono<StreamByCarPlateRequest>): Flux<StreamByCarPlateResponse> {
        return request.flatMapMany { streamByCarPlateRequest ->
            fineService.getFineByCarPlate(streamByCarPlateRequest.carPlate)
                .flatMapMany { initFineState ->
                    natsEventSubscriber.subscribe(streamByCarPlateRequest.carPlate)
                        .map { trafficTicketAddedEvent ->
                            buildStreamByCarPlateResponse(trafficTicketAddedEvent.fine.toFine())
                        }.startWith(buildStreamByCarPlateResponse(initFineState))
                }
                .onErrorResume { exception -> handleCarPlateStreamException(exception) }
        }
    }

    private fun buildAllCarsResponse(cars: List<CarResponse>): GetAllCarsResponse {
        return GetAllCarsResponse.newBuilder()
            .apply { successBuilder.addAllCars(cars.map { it.toCar().toProto() }) }
            .build()
    }

    private fun buildGetFineByCarPlateResponse(fine: Fine): GetFineByCarPlateResponse {
        return GetFineByCarPlateResponse.newBuilder()
            .apply { successBuilder.setFine(fine.toProto()) }
            .build()
    }

    private fun buildStreamByCarPlateResponse(fine: Fine): StreamByCarPlateResponse {
        return StreamByCarPlateResponse.newBuilder()
            .apply { successBuilder.setFine(fine.toProto()) }
            .build()
    }

    private fun handleCarsException(exception: Throwable): Mono<GetAllCarsResponse> =
        GetAllCarsResponse.newBuilder().apply {
            when (exception) {
                is CarsNotFoundException -> failureBuilder.carsNotFoundErrorBuilder
                else -> failureBuilder.setMessage(exception.stackTraceToString())
            }
        }.build().toMono()

    private fun handleCarPlateException(exception: Throwable): Mono<GetFineByCarPlateResponse> =
        GetFineByCarPlateResponse.newBuilder().apply {
            when (exception) {
                is CarPlateNotFoundException -> failureBuilder.carPlateNotFoundErrorBuilder
                else -> failureBuilder.setMessage(exception.stackTraceToString())
            }
        }.build().toMono()

    private fun handleCarPlateStreamException(exception: Throwable): Mono<StreamByCarPlateResponse> =
        StreamByCarPlateResponse.newBuilder().apply {
            when (exception) {
                is CarPlateNotFoundException -> failureBuilder.carPlateNotFoundErrorBuilder
                else -> failureBuilder.setMessage(exception.stackTraceToString())
            }
        }.build().toMono()
}
