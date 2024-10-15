package ua.anastasiia.finesapp.application.service

import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorMap
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.switchIfEmptyDeferred
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.application.exception.CarPlateDuplicateException
import ua.anastasiia.finesapp.application.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.application.exception.CarsNotFoundException
import ua.anastasiia.finesapp.application.exception.FineIdNotFoundException
import ua.anastasiia.finesapp.application.exception.FinesInLocationNotFound
import ua.anastasiia.finesapp.application.exception.NoFinesFoundByDateException
import ua.anastasiia.finesapp.application.exception.NoFinesFoundException
import ua.anastasiia.finesapp.application.exception.TrafficTicketNotFoundException
import ua.anastasiia.finesapp.application.exception.TrafficTicketWithViolationNotFoundException
import ua.anastasiia.finesapp.application.port.input.FineServiceInPort
import ua.anastasiia.finesapp.application.port.output.FineCreatedProducerOutPort
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.application.port.output.TrafficTicketAddedEventProducerOutPort
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.domain.converter.toFine
import ua.anastasiia.finesapp.generateFines
import ua.anastasiia.propertyautofill.annotation.AutofillNullable
import ua.anastasiia.propertyautofill.annotation.NullableGenerate
import ua.anastasiia.propertyautofill.bpp.fieldGeneration.RandomModelGenerator
import java.time.LocalDate

@Service
@NullableGenerate
@Suppress("TooManyFunctions")
class FineService(
    val fineRepository: FineRepositoryOutPort,
    val ticketAddedEventProducerOutPort: TrafficTicketAddedEventProducerOutPort,
    val fineCreatedProducerOutPort: FineCreatedProducerOutPort
) : FineServiceInPort {

    override fun getAllFines(): Flux<Fine> =
        fineRepository.getAllFines()
            .switchIfEmptyDeferred { NoFinesFoundException.toMono() }

    override fun getAllFinesInLocation(
        longitude: Double,
        latitude: Double,
        radiusInMeters: Double
    ): Flux<Fine> =
        fineRepository.getAllFinesInLocation(longitude, latitude, radiusInMeters)
            .switchIfEmptyDeferred { FinesInLocationNotFound(longitude, latitude).toMono() }

    override fun getAllFinesByDate(date: LocalDate): Flux<Fine> =
        fineRepository.getAllFinesByDate(date)
            .switchIfEmptyDeferred { NoFinesFoundByDateException(date).toMono() }

    override fun getFineById(fineId: String): Mono<Fine> {
        return fineRepository.getFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
    }

    override fun getFineByCarPlate(plate: String): Mono<Fine> =
        fineRepository.getFineByCarPlate(plate)
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun saveFine(fine: Fine): Mono<Fine> =
        fineRepository.getFineByCarPlate(fine.car.plate)
            .handle<Fine> { _, sink ->
                sink.error(CarPlateDuplicateException(fine.car.plate))
            }
            .onErrorMap(DuplicateKeyException::class) { CarPlateDuplicateException(fine.car.plate) }
            .switchIfEmpty {
                fineRepository.saveFine(fine).doOnNext {
                    fineCreatedProducerOutPort.sendEvent(it)
                }
            }

    override fun saveFines(mongoFines: List<Fine>): Flux<Fine> =
        fineRepository.saveFines(mongoFines)
            .doOnNext {
                fineCreatedProducerOutPort.sendEvent(it)
            }
            .onErrorMap(DuplicateKeyException::class) {
                CarPlateDuplicateException(mongoFines.joinToString { it.car.plate })
            }

    override fun saveGeneratedFines(number: Int): Flux<Fine> {
        return saveFines(generateFines(number).map { it.toFine() })
    }

    override fun deleteFineById(fineId: String): Mono<Fine> =
        fineRepository.deleteFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }

    override fun addTrafficTicketByCarPlate(plate: String, ticket: Fine.TrafficTicket): Mono<Fine> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.addTrafficTicketByCarPlate(plate, ticket)
                    .doOnNext {
                        ticketAddedEventProducerOutPort.sendEvent(it, ticket)
                    }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: String,
        updatedTicket: Fine.TrafficTicket
    ): Mono<Fine> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.updateTrafficTicketByCarPlateAndId(
                    plate,
                    trafficTicketId,
                    updatedTicket.copy(id = trafficTicketId)
                )
                    .switchIfEmpty { TrafficTicketNotFoundException(plate, trafficTicketId).toMono() }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: String,
        violations: List<Fine.TrafficTicket.Violation>
    ): Mono<Fine> =
        fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.addViolationToTrafficTicket(plate, trafficTicketId, violations)
                    .switchIfEmpty { TrafficTicketNotFoundException(plate, trafficTicketId).toMono() }
            }
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }

    override fun removeViolationFromTicket(
        carPlate: String,
        ticketId: String,
        violationDescription: String
    ): Mono<Fine> = fineRepository.removeViolationFromTicket(
        carPlate,
        ticketId,
        violationDescription
    )
        .switchIfEmpty { TrafficTicketWithViolationNotFoundException(ticketId, violationDescription).toMono() }

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine> =
        fineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)
            .switchIfEmpty { TrafficTicketNotFoundException(carPlate, ticketId).toMono() }

    override fun getSumOfFinesForCarPlate(plate: String): Mono<Double> =
        fineRepository.getSumOfFinesForCarPlate(plate)
            .defaultIfEmpty(0.0)

    override fun getAllCars(): Flux<Fine.Car> =
        fineRepository.getAllCars()
            .switchIfEmptyDeferred { CarsNotFoundException.toMono() }

    override fun updateCarById(
        fineId: String,
        @AutofillNullable(
            fieldToGenerate = "model",
            valueProvider = RandomModelGenerator::class
        )
        car: Fine.Car
    ): Mono<Fine> =
        fineRepository.updateCarById(fineId, car)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
}
