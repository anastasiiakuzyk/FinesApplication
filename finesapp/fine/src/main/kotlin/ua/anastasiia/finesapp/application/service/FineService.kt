package ua.anastasiia.finesapp.application.service

import org.slf4j.LoggerFactory
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
    val fineCreatedProducerOutPort: FineCreatedProducerOutPort,
) : FineServiceInPort {

    override fun getAllFines(): Flux<Fine> {
        LOG.info("Retrieving all fines")
        return fineRepository.getAllFines()
            .switchIfEmptyDeferred { NoFinesFoundException.toMono() }
    }

    override fun getAllFinesInLocation(
        longitude: Double,
        latitude: Double,
        radiusInMeters: Double,
    ): Flux<Fine> {
        LOG.info(
            "Retrieving all fines in location longitude={}, latitude={}, radiusInMeters={}",
            longitude,
            latitude,
            radiusInMeters
        )
        return fineRepository.getAllFinesInLocation(longitude, latitude, radiusInMeters)
            .switchIfEmptyDeferred { FinesInLocationNotFound(longitude, latitude).toMono() }
    }

    override fun getAllFinesByDate(date: LocalDate): Flux<Fine> {
        LOG.info("Retrieving all fines by date {}", date)
        return fineRepository.getAllFinesByDate(date)
            .switchIfEmptyDeferred { NoFinesFoundByDateException(date).toMono() }
    }

    override fun getFineById(fineId: String): Mono<Fine> {
        LOG.info("Retrieving fine by id {}", fineId)
        return fineRepository.getFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
    }

    override fun getFineByCarPlate(plate: String): Mono<Fine> {
        LOG.info("Retrieving fine by car plate {}", plate)
        return fineRepository.getFineByCarPlate(plate)
            .switchIfEmpty { CarPlateNotFoundException(plate).toMono() }
    }

    override fun saveFine(fine: Fine): Mono<Fine> {
        LOG.info("Saving fine for car plate {}", fine.car.plate)
        return fineRepository.getFineByCarPlate(fine.car.plate)
            .flatMap { existingFine ->
                LOG.info("Existing fine found for car plate {}, adding traffic tickets", fine.car.plate)
                Flux.fromIterable(fine.trafficTickets)
                    .flatMap { ticket ->
                        addTrafficTicketByCarPlate(fine.car.plate, ticket)
                    }
                    .then(Mono.just(existingFine))
            }
            .switchIfEmpty {
                LOG.info("No existing fine found for car plate {}, saving new fine", fine.car.plate)
                fineRepository.saveFine(fine).doOnNext {
                    LOG.info("Fine saved for car plate {}", fine.car.plate)
                    fineCreatedProducerOutPort.sendEvent(it)
                }
            }
    }

    override fun saveFines(mongoFines: List<Fine>): Flux<Fine> {
        LOG.info("Saving multiple fines")
        return fineRepository.saveFines(mongoFines)
            .doOnNext {
                LOG.info("Fine saved for car plate {}", it.car.plate)
                fineCreatedProducerOutPort.sendEvent(it)
            }
            .onErrorMap(DuplicateKeyException::class) {
                val plates = mongoFines.joinToString { it.car.plate }
                LOG.error("Duplicate car plate exception for plates {}", plates)
                CarPlateDuplicateException(plates)
            }
    }

    override fun saveGeneratedFines(number: Int): Flux<Fine> {
        LOG.info("Saving {} generated fines", number)
        return saveFines(generateFines(number).map { it.toFine() })
    }

    override fun deleteFineById(fineId: String): Mono<Fine> {
        LOG.info("Deleting fine by id {}", fineId)
        return fineRepository.deleteFineById(fineId)
            .switchIfEmpty { FineIdNotFoundException(fineId).toMono() }
    }

    override fun getFineByCarPlateAndTicketId(carPlate: String, ticketId: String): Mono<Fine> {
        LOG.info("Retrieving fine by car plate {} and ticket id {}", carPlate, ticketId)
        return fineRepository.getFineByCarPlateAndTicketId(carPlate, ticketId)
            .switchIfEmpty { TrafficTicketNotFoundException(carPlate, ticketId).toMono() }
    }

    override fun addTrafficTicketByCarPlate(plate: String, ticket: Fine.TrafficTicket): Mono<Fine> {
        LOG.info("Adding traffic ticket to car plate {}", plate)
        return fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.addTrafficTicketByCarPlate(plate, ticket)
                    .doOnNext {
                        LOG.info("Traffic ticket added to car plate {}", plate)
                        ticketAddedEventProducerOutPort.sendEvent(it, ticket)
                    }
            }
            .switchIfEmpty {
                LOG.error("Car plate {} not found when adding traffic ticket", plate)
                CarPlateNotFoundException(plate).toMono()
            }
    }

    override fun deleteTrafficTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine> {
        LOG.info("Deleting traffic ticket by car plate {} and ticket id {}", carPlate, ticketId)
        return fineRepository.deleteTrafficTicketByCarPlateAndId(carPlate, ticketId)
            .switchIfEmpty { TrafficTicketNotFoundException(carPlate, ticketId).toMono() }
    }

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: String,
        updatedTicket: Fine.TrafficTicket,
    ): Mono<Fine> {
        LOG.info("Updating traffic ticket id {} for car plate {}", trafficTicketId, plate)
        return fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.updateTrafficTicketByCarPlateAndId(
                    plate,
                    trafficTicketId,
                    updatedTicket.copy(id = trafficTicketId)
                )
                    .doOnNext {
                        LOG.info("Traffic ticket id {} updated for car plate {}", trafficTicketId, plate)
                    }
                    .switchIfEmpty {
                        LOG.error("Traffic ticket id {} not found for car plate {}", trafficTicketId, plate)
                        TrafficTicketNotFoundException(plate, trafficTicketId).toMono()
                    }
            }
            .switchIfEmpty {
                LOG.error("Car plate {} not found when updating traffic ticket", plate)
                CarPlateNotFoundException(plate).toMono()
            }
    }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: String,
        violations: List<Fine.TrafficTicket.Violation>,
    ): Mono<Fine> {
        LOG.info("Adding violations to traffic ticket id {} for car plate {}", trafficTicketId, plate)
        return fineRepository.getFineByCarPlate(plate)
            .flatMap {
                fineRepository.addViolationToTrafficTicket(plate, trafficTicketId, violations)
                    .doOnNext {
                        LOG.info("Violations added to traffic ticket id {} for car plate {}", trafficTicketId, plate)
                    }
                    .switchIfEmpty {
                        LOG.error("Traffic ticket id {} not found for car plate {}", trafficTicketId, plate)
                        TrafficTicketNotFoundException(plate, trafficTicketId).toMono()
                    }
            }
            .switchIfEmpty {
                LOG.error("Car plate {} not found when adding violations", plate)
                CarPlateNotFoundException(plate).toMono()
            }
    }

    override fun removeViolationFromTicket(
        carPlate: String,
        ticketId: String,
        violationDescription: String,
    ): Mono<Fine> {
        LOG.info("Removing violation '{}' from ticket id {} for car plate {}", violationDescription, ticketId, carPlate)
        return fineRepository.removeViolationFromTicket(
            carPlate,
            ticketId,
            violationDescription
        )
            .doOnNext {
                LOG.info(
                    "Violation '{}' removed from ticket id {} for car plate {}",
                    violationDescription,
                    ticketId,
                    carPlate
                )
            }
            .switchIfEmpty {
                LOG.error(
                    "Violation '{}' not found in ticket id {} for car plate {}",
                    violationDescription,
                    ticketId,
                    carPlate
                )
                TrafficTicketWithViolationNotFoundException(ticketId, violationDescription).toMono()
            }
    }

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine> {
        LOG.info("Removing ticket id {} from car plate {}", ticketId, carPlate)
        return fineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)
            .switchIfEmpty {
                LOG.error("Ticket id {} not found for car plate {}", ticketId, carPlate)
                TrafficTicketNotFoundException(carPlate, ticketId).toMono()
            }
    }

    override fun getSumOfFinesForCarPlate(plate: String): Mono<Double> {
        LOG.info("Calculating sum of fines for car plate {}", plate)
        return fineRepository.getSumOfFinesForCarPlate(plate)
            .doOnNext { sum ->
                LOG.info("Sum of fines for car plate {} is {}", plate, sum)
            }
            .defaultIfEmpty(0.0)
    }

    override fun getAllCars(): Flux<Fine.Car> {
        LOG.info("Retrieving all cars")
        return fineRepository.getAllCars()
            .switchIfEmptyDeferred { CarsNotFoundException.toMono() }
    }

    override fun updateCarById(
        fineId: String,
        @AutofillNullable(
            fieldToGenerate = "model",
            valueProvider = RandomModelGenerator::class
        )
        car: Fine.Car,
    ): Mono<Fine> {
        LOG.info("Updating car details for fine id {}", fineId)
        return fineRepository.updateCarById(fineId, car)
            .doOnNext {
                LOG.info("Car details updated for fine id {}", fineId)
            }
            .switchIfEmpty {
                LOG.error("Fine id {} not found when updating car details", fineId)
                FineIdNotFoundException(fineId).toMono()
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FineService::class.java)
    }
}
