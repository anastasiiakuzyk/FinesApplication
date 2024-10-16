package ua.anastasiia.finesapp.application.port.input

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.domain.Fine
import java.time.LocalDate

@Suppress("TooManyFunctions")
interface FineServiceInPort {

    fun updateCarById(fineId: String, car: Fine.Car): Mono<Fine>

    fun getAllCars(): Flux<Fine.Car>

    fun getSumOfFinesForCarPlate(plate: String): Mono<Double>

    fun removeViolationFromTicket(carPlate: String, ticketId: String, violationDescription: String): Mono<Fine>

    fun removeTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine>

    fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: String,
        violations: List<Fine.TrafficTicket.Violation>
    ): Mono<Fine>

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: String,
        updatedTicket: Fine.TrafficTicket
    ): Mono<Fine>

    fun addTrafficTicketByCarPlate(plate: String, ticket: Fine.TrafficTicket): Mono<Fine>

    fun deleteTrafficTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine>

    fun deleteFineById(fineId: String): Mono<Fine>

    fun getFineByCarPlateAndTicketId(carPlate: String, ticketId: String): Mono<Fine>

    fun saveFines(mongoFines: List<Fine>): Flux<Fine>

    fun getFineByCarPlate(plate: String): Mono<Fine>

    fun getFineById(fineId: String): Mono<Fine>

    fun getAllFinesByDate(date: LocalDate): Flux<Fine>

    fun getAllFinesInLocation(longitude: Double, latitude: Double, radiusInMeters: Double): Flux<Fine>

    fun getAllFines(): Flux<Fine>

    fun saveFine(fine: Fine): Mono<Fine>

    fun saveGeneratedFines(number: Int): Flux<Fine>
}
