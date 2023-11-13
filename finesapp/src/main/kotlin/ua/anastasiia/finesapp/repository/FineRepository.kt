package ua.anastasiia.finesapp.repository

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.dto.response.CarResponse
import ua.anastasiia.finesapp.dto.response.TotalFineSumResponse
import java.time.LocalDate

@Suppress("TooManyFunctions")
interface FineRepository {

    fun getAllFines(): Flux<Fine>

    fun getAllFinesInLocation(longitude: Double, latitude: Double, radiusInMeters: Double): Flux<Fine>

    fun getAllFinesByDate(localDate: LocalDate): Flux<Fine>

    fun getFineById(fineId: ObjectId): Mono<Fine>

    fun getFineByCarPlate(plate: String): Mono<Fine>

    fun saveFine(mongoFine: Fine): Mono<Fine>

    fun saveFines(mongoFines: List<Fine>): Flux<Fine>

    fun deleteFineById(fineId: ObjectId): Mono<Fine>

    fun addTrafficTicketByCarPlate(plate: String, newTicket: Fine.TrafficTicket): Mono<Fine>

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicket: Fine.TrafficTicket
    ): Mono<Fine>

    fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: ObjectId,
        violations: List<Fine.TrafficTicket.Violation>
    ): Mono<Fine>

    fun removeViolationFromTicket(carPlate: String, ticketId: ObjectId, violationDescription: String): Mono<Fine>

    fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): Mono<Fine>

    fun getSumOfFinesForCarPlate(carPlate: String): Mono<TotalFineSumResponse>

    fun getAllCars(): Flux<CarResponse>

    fun updateCarById(fineId: ObjectId, car: Fine.Car): Mono<Fine>
}