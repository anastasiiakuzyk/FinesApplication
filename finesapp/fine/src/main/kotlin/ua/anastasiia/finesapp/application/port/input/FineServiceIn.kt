package ua.anastasiia.finesapp.application.port.input

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.infrastructure.dto.request.CarRequest
import ua.anastasiia.finesapp.infrastructure.dto.request.FineRequest
import ua.anastasiia.finesapp.infrastructure.dto.request.TrafficTicketRequest
import ua.anastasiia.finesapp.infrastructure.dto.response.CarResponse
import ua.anastasiia.finesapp.infrastructure.dto.response.FineResponse
import ua.anastasiia.finesapp.infrastructure.dto.response.TotalFineSumResponse
import java.time.LocalDate

@Suppress("TooManyFunctions")
interface FineServiceIn {

    fun updateCarById(fineId: String, carRequest: CarRequest): Mono<FineResponse>

    fun getAllCars(): Flux<CarResponse>

    fun getSumOfFinesForCarPlate(plate: String): Mono<TotalFineSumResponse>

    fun removeViolationFromTicket(carPlate: String, ticketId: String, violationId: Int): Mono<FineResponse>

    fun removeTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<FineResponse>

    fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: String,
        violationIds: List<Int>
    ): Mono<FineResponse>

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: String,
        updatedTicketRequest: TrafficTicketRequest
    ): Mono<FineResponse>

    fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): Mono<FineResponse>

    fun deleteFineById(fineId: String): Mono<FineResponse>

    fun saveFines(mongoFines: List<FineRequest>): Flux<FineResponse>

    fun getFineByCarPlate(plate: String): Mono<FineResponse>

    fun getFineById(fineId: String): Mono<FineResponse>

    fun getAllFinesByDate(date: LocalDate): Flux<FineResponse>

    fun getAllFinesInLocation(longitude: Double, latitude: Double, radiusInMeters: Double): Flux<FineResponse>

    fun getAllFines(): Flux<FineResponse>

    fun saveFine(fineRequest: FineRequest): Mono<FineResponse>
}
