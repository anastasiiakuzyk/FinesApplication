package ua.anastasiia.finesapp.service

import org.bson.types.ObjectId
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorMap
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.switchIfEmptyDeferred
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.annotation.AutofillNullable
import ua.anastasiia.finesapp.annotation.NullableGenerate
import ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration.RandomModelGenerator
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.domain.toDomainCar
import ua.anastasiia.finesapp.domain.toDomainFine
import ua.anastasiia.finesapp.domain.toDomainTrafficTicket
import ua.anastasiia.finesapp.domain.toDomainViolation
import ua.anastasiia.finesapp.domain.toMongoFine
import ua.anastasiia.finesapp.dto.request.CarRequest
import ua.anastasiia.finesapp.dto.request.FineRequest
import ua.anastasiia.finesapp.dto.request.TrafficTicketRequest
import ua.anastasiia.finesapp.dto.request.toCar
import ua.anastasiia.finesapp.dto.request.toFine
import ua.anastasiia.finesapp.dto.request.toTrafficTicket
import ua.anastasiia.finesapp.dto.response.CarResponse
import ua.anastasiia.finesapp.dto.response.FineResponse
import ua.anastasiia.finesapp.dto.response.TotalFineSumResponse
import ua.anastasiia.finesapp.dto.response.toResponse
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.dto.toViolation
import ua.anastasiia.finesapp.dto.toViolationType
import ua.anastasiia.finesapp.exception.CarPlateDuplicateException
import ua.anastasiia.finesapp.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.exception.CarsNotFoundException
import ua.anastasiia.finesapp.exception.FineIdNotFoundException
import ua.anastasiia.finesapp.exception.FinesInLocationNotFound
import ua.anastasiia.finesapp.exception.NoFinesFoundByDateException
import ua.anastasiia.finesapp.exception.NoFinesFoundException
import ua.anastasiia.finesapp.exception.TrafficTicketNotFoundException
import ua.anastasiia.finesapp.exception.TrafficTicketWithViolationNotFoundException
import ua.anastasiia.finesapp.kafka.FineKafkaProducer
import ua.anastasiia.finesapp.repository.FineRepository
import java.time.LocalDate

@Service
@NullableGenerate
@Suppress("TooManyFunctions")
class FineServiceImpl(
    val fineRepository: FineRepository,
    val fineKafkaProducer: FineKafkaProducer
) : FineService {
    override fun getAllFines(): Flux<FineResponse> =
        fineRepository.getAllFines()
            .switchIfEmptyDeferred { NoFinesFoundException.toMono() }
            .map { it.toMongoFine().toResponse() }

    override fun getAllFinesInLocation(
        longitude: Double,
        latitude: Double,
        radiusInMeters: Double
    ): Flux<FineResponse> =
        fineRepository.getAllFinesInLocation(longitude, latitude, radiusInMeters)
            .switchIfEmptyDeferred { FinesInLocationNotFound(longitude, latitude).toMono() }
            .map { it.toMongoFine().toResponse() }

    override fun getAllFinesByDate(date: LocalDate): Flux<FineResponse> =
        fineRepository.getAllFinesByDate(date)
            .switchIfEmptyDeferred { NoFinesFoundByDateException(date).toMono() }
            .map { it.toMongoFine().toResponse() }

    override fun getFineById(fineId: ObjectId): Mono<FineResponse> {
        return fineRepository.getFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toMongoFine().toResponse() }
    }

    override fun getFineByCarPlate(plate: String): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(plate)
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }
            .map { it.toMongoFine().toResponse() }

    override fun saveFine(fineRequest: FineRequest): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(fineRequest.car.plate)
            .handle<Fine> { _, sink ->
                sink.error(CarPlateDuplicateException(fineRequest.car.plate))
            }
            .onErrorMap(DuplicateKeyException::class) { CarPlateDuplicateException(fineRequest.car.plate) }
            .switchIfEmpty { fineRepository.saveFine(fineRequest.toFine().toDomainFine()) }
            .map { it.toMongoFine().toResponse() }

    override fun saveFines(mongoFines: List<FineRequest>): Flux<FineResponse> =
        fineRepository.saveFines(mongoFines.map { it.toFine().toDomainFine() })
            .onErrorMap(DuplicateKeyException::class) {
                CarPlateDuplicateException(mongoFines.joinToString { it.car.plate })
            }
            .map { it.toMongoFine().toResponse() }

    override fun deleteFineById(fineId: ObjectId): Mono<FineResponse> =
        fineRepository.deleteFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toMongoFine().toResponse() }

    override fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.addTrafficTicketByCarPlate(
                    plate,
                    ticketRequest.toTrafficTicket().toDomainTrafficTicket()
                )
                    .doOnNext {
                        fineKafkaProducer.produceNotification(
                            it.toMongoFine().toProto(),
                            ticketRequest.toTrafficTicket().toProto().id
                        )
                    }
                    .map { it.toMongoFine().toResponse() }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicketRequest: TrafficTicketRequest
    ): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.updateTrafficTicketByCarPlateAndId(
                    plate,
                    trafficTicketId,
                    updatedTicketRequest.copy(id = trafficTicketId).toTrafficTicket().toDomainTrafficTicket()
                )
                    .switchIfEmpty { TrafficTicketNotFoundException(plate, trafficTicketId).toMono() }
                    .map { it.toMongoFine().toResponse() }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: ObjectId,
        violationIds: List<Int>
    ): Mono<FineResponse> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.addViolationToTrafficTicket(
                    plate,
                    trafficTicketId,
                    violationIds.map { violationId ->
                        violationId.toViolationType().toViolation().toDomainViolation()
                    }
                )
                    .switchIfEmpty { TrafficTicketNotFoundException(plate, trafficTicketId).toMono() }
                    .map { it.toMongoFine().toResponse() }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun removeViolationFromTicket(carPlate: String, ticketId: ObjectId, violationId: Int): Mono<FineResponse> =
        fineRepository.removeViolationFromTicket(
            carPlate,
            ticketId,
            violationId.toViolationType().toViolation().description
        )
            .switchIfEmpty { TrafficTicketWithViolationNotFoundException(ticketId, violationId).toMono() }
            .map { it.toMongoFine().toResponse() }

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): Mono<FineResponse> =
        fineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)
            .switchIfEmpty { TrafficTicketNotFoundException(carPlate, ticketId).toMono() }
            .map { it.toMongoFine().toResponse() }

    override fun getSumOfFinesForCarPlate(plate: String): Mono<TotalFineSumResponse> =
        fineRepository.getSumOfFinesForCarPlate(plate)
            .defaultIfEmpty(TotalFineSumResponse(plate, 0.0))

    override fun getAllCars(): Flux<CarResponse> =
        fineRepository.getAllCars()
            .switchIfEmptyDeferred { CarsNotFoundException.toMono() }

    override fun updateCarById(
        fineId: ObjectId,
        @AutofillNullable(
            fieldToGenerate = "model",
            valueProvider = RandomModelGenerator::class
        )
        carRequest: CarRequest
    ): Mono<FineResponse> =
        fineRepository.updateCarById(fineId, carRequest.toCar().toDomainCar())
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toMongoFine().toResponse() }
}
