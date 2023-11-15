package ua.anastasiia.finesapp.application.port.output

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.domain.Fine
import java.time.LocalDate

@Suppress("TooManyFunctions")
interface FineRepositoryOut {

    fun getAllFines(): Flux<Fine>

    fun getAllFinesInLocation(longitude: Double, latitude: Double, radiusInMeters: Double): Flux<Fine>

    fun getAllFinesByDate(localDate: LocalDate): Flux<Fine>

    fun getFineById(fineId: String): Mono<Fine>

    fun getFineByCarPlate(plate: String): Mono<Fine>

    fun saveFine(fine: Fine): Mono<Fine>

    fun saveFines(fines: List<Fine>): Flux<Fine>

    fun deleteFineById(fineId: String): Mono<Fine>

    fun addTrafficTicketByCarPlate(plate: String, newTicket: Fine.TrafficTicket): Mono<Fine>

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: String,
        updatedTicket: Fine.TrafficTicket
    ): Mono<Fine>

    fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: String,
        violations: List<Fine.TrafficTicket.Violation>
    ): Mono<Fine>

    fun removeViolationFromTicket(carPlate: String, ticketId: String, violationDescription: String): Mono<Fine>

    fun removeTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine>

    fun getSumOfFinesForCarPlate(carPlate: String): Mono<Double>

    fun getAllCars(): Flux<Fine.Car>

    fun updateCarById(fineId: String, car: Fine.Car): Mono<Fine>
}
