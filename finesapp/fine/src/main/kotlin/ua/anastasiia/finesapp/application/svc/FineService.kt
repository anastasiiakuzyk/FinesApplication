package ua.anastasiia.finesapp.application.svc

import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorMap
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.switchIfEmptyDeferred
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.application.annotation.AutofillNullable
import ua.anastasiia.finesapp.application.annotation.NullableGenerate
import ua.anastasiia.finesapp.application.exception.CarPlateDuplicateException
import ua.anastasiia.finesapp.application.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.application.exception.CarsNotFoundException
import ua.anastasiia.finesapp.application.exception.FineIdNotFoundException
import ua.anastasiia.finesapp.application.exception.FinesInLocationNotFound
import ua.anastasiia.finesapp.application.exception.NoFinesFoundByDateException
import ua.anastasiia.finesapp.application.exception.NoFinesFoundException
import ua.anastasiia.finesapp.application.exception.TrafficTicketNotFoundException
import ua.anastasiia.finesapp.application.exception.TrafficTicketWithViolationNotFoundException
import ua.anastasiia.finesapp.application.port.input.FineServiceIn
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOut
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.adapters.kafka.FineKafkaProducer
import ua.anastasiia.finesapp.infrastructure.config.bpp.fieldGeneration.RandomModelGenerator
import ua.anastasiia.finesapp.infrastructure.dto.request.CarRequest
import ua.anastasiia.finesapp.infrastructure.dto.request.FineRequest
import ua.anastasiia.finesapp.infrastructure.dto.request.TrafficTicketRequest
import ua.anastasiia.finesapp.infrastructure.dto.response.CarResponse
import ua.anastasiia.finesapp.infrastructure.dto.response.FineResponse
import ua.anastasiia.finesapp.infrastructure.dto.response.TotalFineSumResponse
import ua.anastasiia.finesapp.infrastructure.mapper.toCar
import ua.anastasiia.finesapp.infrastructure.mapper.toFine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.infrastructure.mapper.toResponse
import ua.anastasiia.finesapp.infrastructure.mapper.toTrafficTicket
import ua.anastasiia.finesapp.infrastructure.mapper.toViolation
import ua.anastasiia.finesapp.infrastructure.mapper.toViolationType
import java.time.LocalDate

@Service
@NullableGenerate
@Suppress("TooManyFunctions")
class FineService(
    val fineRepository: FineRepositoryOut,
    val fineKafkaProducer: FineKafkaProducer
) : FineServiceIn {
    override fun getAllFines(): Flux<FineResponse> =
        fineRepository.getAllFines()
            .switchIfEmptyDeferred { NoFinesFoundException.toMono() }
            .map { it.toResponse() }

    override fun getAllFinesInLocation(
        longitude: Double,
        latitude: Double,
        radiusInMeters: Double
    ): Flux<FineResponse> =
        fineRepository.getAllFinesInLocation(longitude, latitude, radiusInMeters)
            .switchIfEmptyDeferred { FinesInLocationNotFound(longitude, latitude).toMono() }
            .map { it.toResponse() }

    override fun getAllFinesByDate(date: LocalDate): Flux<FineResponse> =
        fineRepository.getAllFinesByDate(date)
            .switchIfEmptyDeferred { NoFinesFoundByDateException(date).toMono() }
            .map { it.toResponse() }

    override fun getFineById(fineId: String): Mono<FineResponse> {
        return fineRepository.getFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toResponse() }
    }

    override fun getFineByCarPlate(plate: String): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(plate)
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }
            .map { it.toResponse() }

    override fun saveFine(fineRequest: FineRequest): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(fineRequest.car.plate)
            .handle<Fine> { _, sink ->
                sink.error(CarPlateDuplicateException(fineRequest.car.plate))
            }
            .onErrorMap(DuplicateKeyException::class) { CarPlateDuplicateException(fineRequest.car.plate) }
            .switchIfEmpty { fineRepository.saveFine(fineRequest.toFine()) }
            .map { it.toResponse() }

    override fun saveFines(mongoFines: List<FineRequest>): Flux<FineResponse> =
        fineRepository.saveFines(mongoFines.map { it.toFine() })
            .onErrorMap(DuplicateKeyException::class) {
                CarPlateDuplicateException(mongoFines.joinToString { it.car.plate })
            }
            .map { it.toResponse() }

    override fun deleteFineById(fineId: String): Mono<FineResponse> =
        fineRepository.deleteFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toResponse() }

    override fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.addTrafficTicketByCarPlate(
                    plate,
                    ticketRequest.toTrafficTicket()
                )
                    .doOnNext {
                        fineKafkaProducer.produceNotification(
                            it.toProto(),
                            ticketRequest.toTrafficTicket().toProto().id
                        )
                    }
                    .map { it.toResponse() }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: String,
        updatedTicketRequest: TrafficTicketRequest
    ): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.updateTrafficTicketByCarPlateAndId(
                    plate,
                    trafficTicketId,
                    updatedTicketRequest.copy(id = trafficTicketId).toTrafficTicket()
                )
                    .switchIfEmpty { TrafficTicketNotFoundException(plate, trafficTicketId).toMono() }
                    .map { it.toResponse() }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: String,
        violationIds: List<Int>
    ): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.addViolationToTrafficTicket(
                    plate,
                    trafficTicketId,
                    violationIds.map { violationId ->
                        violationId.toViolationType().toViolation()
                    }
                )
                    .switchIfEmpty { TrafficTicketNotFoundException(plate, trafficTicketId).toMono() }
                    .map { it.toResponse() }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun removeViolationFromTicket(carPlate: String, ticketId: String, violationId: Int): Mono<FineResponse> =
        fineRepository.removeViolationFromTicket(
            carPlate,
            ticketId,
            violationId.toViolationType().toViolation().description
        )
            .switchIfEmpty { TrafficTicketWithViolationNotFoundException(ticketId, violationId).toMono() }
            .map { it.toResponse() }

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<FineResponse> =
        fineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)
            .switchIfEmpty { TrafficTicketNotFoundException(carPlate, ticketId).toMono() }
            .map { it.toResponse() }

    override fun getSumOfFinesForCarPlate(plate: String): Mono<TotalFineSumResponse> =
        fineRepository.getSumOfFinesForCarPlate(plate)
            .defaultIfEmpty(TotalFineSumResponse(plate, 0.0))

    override fun getAllCars(): Flux<CarResponse> =
        fineRepository.getAllCars()
            .switchIfEmptyDeferred { CarsNotFoundException.toMono() }

    override fun updateCarById(
        fineId: String,
        @AutofillNullable(
            fieldToGenerate = "model",
            valueProvider = RandomModelGenerator::class
        )
        carRequest: CarRequest
    ): Mono<FineResponse> =
        fineRepository.updateCarById(fineId, carRequest.toCar())
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toResponse() }
}
