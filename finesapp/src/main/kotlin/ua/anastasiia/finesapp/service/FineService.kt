package ua.anastasiia.finesapp.service

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.dto.request.CarRequest
import ua.anastasiia.finesapp.dto.request.FineRequest
import ua.anastasiia.finesapp.dto.request.TrafficTicketRequest
import ua.anastasiia.finesapp.dto.response.CarResponse
import ua.anastasiia.finesapp.dto.response.FineResponse
import ua.anastasiia.finesapp.dto.response.TotalFineSumResponse
import java.time.LocalDate

@Suppress("TooManyFunctions")
interface FineService {

    fun updateCarById(fineId: ObjectId, carRequest: CarRequest): Mono<FineResponse>

    fun getAllCars(): Flux<CarResponse>

    fun getSumOfFinesForCarPlate(plate: String): Mono<TotalFineSumResponse>

    fun removeViolationFromTicket(carPlate: String, ticketId: ObjectId, violationId: Int): Mono<FineResponse>

    fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): Mono<FineResponse>

    fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: ObjectId,
        violationIds: List<Int>
    ): Mono<FineResponse>

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicketRequest: TrafficTicketRequest
    ): Mono<FineResponse>

    fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): Mono<FineResponse>

    fun deleteFineById(fineId: ObjectId): Mono<FineResponse>

    fun saveFines(mongoFines: List<FineRequest>): Flux<FineResponse>

    fun getFineByCarPlate(plate: String): Mono<FineResponse>

    fun getFineById(fineId: ObjectId): Mono<FineResponse>

    fun getAllFinesByDate(date: LocalDate): Flux<FineResponse>

    fun getAllFinesInLocation(longitude: Double, latitude: Double, radiusInMeters: Double): Flux<FineResponse>

    fun getAllFines(): Flux<FineResponse>

    fun saveFine(fineRequest: FineRequest): Mono<FineResponse>
}
