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
import ua.anastasiia.finesapp.entity.MongoFine
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
import ua.anastasiia.finesapp.repository.MongoFineRepository
import java.time.LocalDate

@Service
@NullableGenerate
@Suppress("TooManyFunctions")
class FineServiceImpl(
    val mongoFineRepository: MongoFineRepository,
    val fineKafkaProducer: FineKafkaProducer
) : FineService {
    override fun getAllFines(): Flux<FineResponse> =
        mongoFineRepository.getAllFines()
            .switchIfEmptyDeferred { NoFinesFoundException.toMono() }
            .map { it.toResponse() }

    override fun getAllFinesInLocation(
        longitude: Double,
        latitude: Double,
        radiusInMeters: Double
    ): Flux<FineResponse> =
        mongoFineRepository.getAllFinesInLocation(longitude, latitude, radiusInMeters)
            .switchIfEmptyDeferred { FinesInLocationNotFound(longitude, latitude).toMono() }
            .map { it.toResponse() }

    override fun getAllFinesByDate(date: LocalDate): Flux<FineResponse> =
        mongoFineRepository.getAllFinesByDate(date)
            .switchIfEmptyDeferred { NoFinesFoundByDateException(date).toMono() }
            .map { it.toResponse() }

    override fun getFineById(fineId: ObjectId): Mono<FineResponse> {
        return mongoFineRepository.getFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toResponse() }
    }

    override fun getFineByCarPlate(plate: String): Mono<FineResponse> =
        mongoFineRepository.getFineByCarPlate(plate)
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }
            .map { it.toResponse() }

    override fun saveFine(fineRequest: FineRequest): Mono<FineResponse> =
        mongoFineRepository.getFineByCarPlate(fineRequest.car.plate)
            .handle<MongoFine> { _, sink ->
                sink.error(CarPlateDuplicateException(fineRequest.car.plate))
            }
            .onErrorMap(DuplicateKeyException::class) { CarPlateDuplicateException(fineRequest.car.plate) }
            .switchIfEmpty { mongoFineRepository.saveFine(fineRequest.toFine()) }
            .map { it.toResponse() }

    override fun saveFines(mongoFines: List<FineRequest>): Flux<FineResponse> =
        mongoFineRepository.saveFines(mongoFines.map { it.toFine() })
            .onErrorMap(DuplicateKeyException::class) {
                CarPlateDuplicateException(mongoFines.joinToString { it.car.plate })
            }
            .map { it.toResponse() }

    override fun deleteFineById(fineId: ObjectId): Mono<FineResponse> =
        mongoFineRepository.deleteFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toResponse() }

    override fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): Mono<FineResponse> =
        mongoFineRepository.getFineByCarPlate(plate)
            .flatMap {
                mongoFineRepository.addTrafficTicketByCarPlate(plate, ticketRequest.toTrafficTicket())
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
        trafficTicketId: ObjectId,
        updatedTicketRequest: TrafficTicketRequest
    ): Mono<FineResponse> =
        mongoFineRepository.getFineByCarPlate(plate)
            .flatMap {
                mongoFineRepository.updateTrafficTicketByCarPlateAndId(
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
        trafficTicketId: ObjectId,
        violationIds: List<Int>
    ): Mono<FineResponse> =
        mongoFineRepository.getFineByCarPlate(plate)
            .flatMap {
                mongoFineRepository.addViolationToTrafficTicket(
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

    override fun removeViolationFromTicket(carPlate: String, ticketId: ObjectId, violationId: Int): Mono<FineResponse> =
        mongoFineRepository.removeViolationFromTicket(
            carPlate,
            ticketId,
            violationId.toViolationType().toViolation().description
        )
            .switchIfEmpty { TrafficTicketWithViolationNotFoundException(ticketId, violationId).toMono() }
            .map { it.toResponse() }

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): Mono<FineResponse> =
        mongoFineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)
            .switchIfEmpty { TrafficTicketNotFoundException(carPlate, ticketId).toMono() }
            .map { it.toResponse() }

    override fun getSumOfFinesForCarPlate(plate: String): Mono<TotalFineSumResponse> =
        mongoFineRepository.getSumOfFinesForCarPlate(plate)
            .defaultIfEmpty(TotalFineSumResponse(plate, 0.0))

    override fun getAllCars(): Flux<CarResponse> =
        mongoFineRepository.getAllCars()
            .switchIfEmptyDeferred { CarsNotFoundException.toMono() }

    override fun updateCarById(
        fineId: ObjectId,
        @AutofillNullable(
            fieldToGenerate = "model",
            valueProvider = RandomModelGenerator::class
        )
        carRequest: CarRequest
    ): Mono<FineResponse> =
        mongoFineRepository.updateCarById(fineId, carRequest.toCar())
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
            .map { it.toResponse() }
}
