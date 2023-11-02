package ua.anastasiia.finesapp.service

import org.bson.types.ObjectId
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
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
import ua.anastasiia.finesapp.repository.MongoFineRepository
import java.time.LocalDate

@Service
@NullableGenerate
@Suppress("TooManyFunctions")
class FineServiceImpl(val mongoFineRepository: MongoFineRepository) : FineService {
    override fun getAllFines(): Flux<FineResponse> =
        mongoFineRepository.getAllFines().map { it.toResponse() }.switchIfEmpty(NoFinesFoundException.toMono())

    override fun getAllFinesInLocation(
        longitude: Double,
        latitude: Double,
        radiusInMeters: Double
    ): Flux<FineResponse> =
        mongoFineRepository.getAllFinesInLocation(longitude, latitude, radiusInMeters).map { it.toResponse() }
            .switchIfEmpty(FinesInLocationNotFound(longitude, latitude).toMono())

    override fun getAllFinesByDate(date: LocalDate): Flux<FineResponse> =
        mongoFineRepository.getAllFinesByDate(date)
            .map { it.toResponse() }
            .switchIfEmpty(NoFinesFoundByDateException(date).toFlux<FineResponse>())

    override fun getFineById(fineId: ObjectId): Mono<FineResponse> {
        return mongoFineRepository.getFineById(fineId)
            .flatMap { fine ->
                fine.toResponse().toMono()
            }
            .switchIfEmpty(FineIdNotFoundException(fineId).toMono())
    }

    override fun getFineByCarPlate(plate: String): Mono<FineResponse> =
        mongoFineRepository.getFineByCarPlate(plate)
            .map { it.toResponse() }
            .switchIfEmpty(CarPlateNotFoundException(plate).toMono())

    override fun saveFine(fineRequest: FineRequest): Mono<FineResponse> =
        mongoFineRepository.getFineByCarPlate(fineRequest.car.plate)
            .handle<MongoFine> { _, sink ->
                sink.error(CarPlateDuplicateException(fineRequest.car.plate))
            }
            .onErrorMap { exception ->
                when (exception) {
                    is DuplicateKeyException -> CarPlateDuplicateException(fineRequest.car.plate)
                    else -> exception
                }
            }
            .switchIfEmpty {
                mongoFineRepository.saveFine(fineRequest.toFine())
            }
            .map { it.toResponse() }

    override fun saveFines(mongoFines: List<FineRequest>): Flux<FineResponse> =
        mongoFineRepository.saveFines(mongoFines.map { it.toFine() })
            .onErrorMap { exception ->
                when (exception) {
                    is DuplicateKeyException ->
                        throw CarPlateDuplicateException(mongoFines.joinToString { it.car.plate })

                    else -> throw exception
                }
            }
            .map { it.toResponse() }

    override fun deleteFineById(fineId: ObjectId): Mono<FineResponse> =
        mongoFineRepository.deleteFineById(fineId)
            .map { it.toResponse() }
            .switchIfEmpty(FineIdNotFoundException(fineId).toMono())

    override fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): Mono<FineResponse> =
        mongoFineRepository.getFineByCarPlate(plate)
            .flatMap {
                mongoFineRepository.addTrafficTicketByCarPlate(plate, ticketRequest.toTrafficTicket())
                    .map { it.toResponse() }
            }
            .switchIfEmpty(CarPlateNotFoundException(plate).toMono())

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
                    .map { it.toResponse() }
                    .switchIfEmpty(TrafficTicketNotFoundException(plate, trafficTicketId).toMono())
            }
            .switchIfEmpty(CarPlateNotFoundException(plate).toMono())

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
                    .map { it.toResponse() }
                    .switchIfEmpty(TrafficTicketNotFoundException(plate, trafficTicketId).toMono())
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun removeViolationFromTicket(carPlate: String, ticketId: ObjectId, violationId: Int): Mono<FineResponse> =
        mongoFineRepository.removeViolationFromTicket(
            carPlate,
            ticketId,
            violationId.toViolationType().toViolation().description
        )
            .map { it.toResponse() }
            .switchIfEmpty(TrafficTicketWithViolationNotFoundException(ticketId, violationId).toMono())

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): Mono<FineResponse> =
        mongoFineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)
            .map { it.toResponse() }
            .switchIfEmpty(TrafficTicketNotFoundException(carPlate, ticketId).toMono())

    override fun getSumOfFinesForCarPlate(plate: String): Mono<TotalFineSumResponse> =
        mongoFineRepository.getSumOfFinesForCarPlate(plate)
            .defaultIfEmpty(TotalFineSumResponse(plate, 0.0))

    override fun getAllCars(): Flux<CarResponse> =
        mongoFineRepository.getAllCars()
            .switchIfEmpty(CarsNotFoundException.toMono())

    override fun updateCarById(
        fineId: ObjectId,
        @AutofillNullable(
            fieldToGenerate = "model",
            valueProvider = RandomModelGenerator::class
        )
        carRequest: CarRequest
    ): Mono<FineResponse> =
        mongoFineRepository.updateCarById(fineId, carRequest.toCar())
            .map { it.toResponse() }
            .switchIfEmpty(FineIdNotFoundException(fineId).toMono())
}
