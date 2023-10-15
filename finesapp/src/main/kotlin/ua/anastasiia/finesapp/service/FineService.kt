package ua.anastasiia.finesapp.service

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.dto.request.CarRequest
import ua.anastasiia.finesapp.dto.response.CarResponse
import ua.anastasiia.finesapp.dto.request.FineRequest
import ua.anastasiia.finesapp.dto.response.FineResponse
import ua.anastasiia.finesapp.dto.response.TotalFineSumResponse
import ua.anastasiia.finesapp.dto.request.TrafficTicketRequest
import java.time.LocalDate

@Suppress("TooManyFunctions")
interface FineService {

    fun updateCarById(fineId: ObjectId, carRequest: CarRequest): FineResponse

    fun getAllCars(): List<CarResponse>

    fun getSumOfFinesForCarPlate(plate: String): TotalFineSumResponse

    fun removeViolationFromTicket(carPlate: String, ticketId: ObjectId, violationId: Int): FineResponse
    fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): FineResponse

    fun addViolationToTrafficTicket(plate: String, trafficTicketId: ObjectId, violationIds: List<Int>): FineResponse

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicketRequest: TrafficTicketRequest
    ): FineResponse

    fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): FineResponse

    fun deleteFineById(fineId: ObjectId): FineResponse

    fun saveFines(mongoFines: List<FineRequest>): List<FineResponse>

    fun getFineByCarPlate(plate: String): FineResponse

    fun getFineById(fineId: ObjectId): FineResponse

    fun getAllFinesByDate(date: LocalDate): List<FineResponse>

    fun getAllFinesInLocation(longitude: Double, latitude: Double, radiusInMeters: Double): List<FineResponse>

    fun getAllFines(): List<FineResponse>

    fun saveFine(fineRequest: FineRequest): FineResponse
}
