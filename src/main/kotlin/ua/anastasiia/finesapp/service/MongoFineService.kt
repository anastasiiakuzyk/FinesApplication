package ua.anastasiia.finesapp.service

import org.bson.types.ObjectId
import ua.anastasiia.finesapp.dto.CarRequest
import ua.anastasiia.finesapp.dto.FineRequest
import ua.anastasiia.finesapp.dto.TotalFineSumResponse
import ua.anastasiia.finesapp.dto.TrafficTicketRequest
import ua.anastasiia.finesapp.entity.MongoFine
import java.time.LocalDate

@Suppress("TooManyFunctions")
interface MongoFineService {

    fun updateCarById(fineId: ObjectId, carRequest: CarRequest): MongoFine

    fun getAllCars(): List<MongoFine.Car>

    fun getSumOfFinesForCarPlate(plate: String): TotalFineSumResponse

    fun removeViolationFromTicket(ticketId: ObjectId, violationId: Int): MongoFine

    fun addViolationToTrafficTicket(plate: String, trafficTicketId: ObjectId, violationIds: List<Int>): MongoFine

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicketRequest: TrafficTicketRequest
    ): MongoFine

    fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): MongoFine

    fun deleteFineById(fineId: ObjectId): MongoFine

    fun saveFines(mongoFines: List<FineRequest>): List<MongoFine>

    fun getFineByCarPlate(plate: String): MongoFine

    fun getFineById(fineId: ObjectId): MongoFine

    fun getAllFinesByDate(date: LocalDate): List<MongoFine>

    fun getAllFinesInLocation(longitude: Double, latitude: Double): List<MongoFine>

    fun getAllFines(): List<MongoFine>

    fun saveFine(fineRequest: FineRequest): MongoFine
}
