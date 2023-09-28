package ua.anastasiia.finesapp.service

import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import ua.anastasiia.finesapp.annotation.AutofillNullable
import ua.anastasiia.finesapp.annotation.NullableGenerate
import ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration.RandomModelGenerator
import ua.anastasiia.finesapp.dto.CarRequest
import ua.anastasiia.finesapp.dto.FineRequest
import ua.anastasiia.finesapp.dto.TotalFineSumResponse
import ua.anastasiia.finesapp.dto.TrafficTicketRequest
import ua.anastasiia.finesapp.dto.Violation
import ua.anastasiia.finesapp.dto.getViolationsByIndexes
import ua.anastasiia.finesapp.dto.toMongoCar
import ua.anastasiia.finesapp.dto.toMongoFine
import ua.anastasiia.finesapp.dto.toMongoTrafficTicket
import ua.anastasiia.finesapp.dto.toMongoViolation
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
class MongoFineServiceImpl(val mongoFineRepository: MongoFineRepository) : MongoFineService {
    override fun getAllFines(): List<MongoFine> =
        mongoFineRepository.getAllFines().ifEmpty { throw NoFinesFoundException() }

    override fun getAllFinesInLocation(longitude: Double, latitude: Double): List<MongoFine> =
        mongoFineRepository.getAllFinesInLocation(longitude, latitude)
            .ifEmpty { throw FinesInLocationNotFound(longitude, latitude) }

    override fun getAllFinesByDate(date: LocalDate): List<MongoFine> =
        mongoFineRepository.getAllFinesByDate(date).ifEmpty { throw NoFinesFoundByDateException(date) }

    override fun getFineById(fineId: ObjectId): MongoFine =
        mongoFineRepository.getFineById(fineId) ?: throw FineIdNotFoundException(fineId)

    override fun getFineByCarPlate(plate: String): MongoFine =
        mongoFineRepository.getFineByCarPlate(plate) ?: throw CarPlateNotFoundException(plate)

    @Suppress("SwallowedException")
    override fun saveFine(fineRequest: FineRequest): MongoFine =
        runCatching {
            mongoFineRepository.saveFine(fineRequest.toMongoFine())
        }.getOrElse {
            throw CarPlateDuplicateException(fineRequest.carRequest.plate)
        }

    override fun saveFines(mongoFines: List<FineRequest>): List<MongoFine> =
        runCatching {
            mongoFineRepository.saveFines(mongoFines.map { it.toMongoFine() })
        }.getOrElse {
            throw CarPlateDuplicateException(mongoFines.joinToString { it.carRequest.plate })
        }

    override fun deleteFineById(fineId: ObjectId): MongoFine =
        mongoFineRepository.deleteFineById(fineId) ?: throw FineIdNotFoundException(fineId)

    override fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): MongoFine =
        mongoFineRepository.getFineByCarPlate(plate)?.let {
            mongoFineRepository.addTrafficTicketByCarPlate(plate, ticketRequest.toMongoTrafficTicket())
        } ?: throw CarPlateNotFoundException(plate)

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicketRequest: TrafficTicketRequest
    ): MongoFine {
        mongoFineRepository.getFineByCarPlate(plate)
            ?: throw CarPlateNotFoundException(plate)
        return mongoFineRepository.updateTrafficTicketByCarPlateAndId(
            plate,
            trafficTicketId,
            updatedTicketRequest.copy(id = trafficTicketId).toMongoTrafficTicket()
        ) ?: throw TrafficTicketNotFoundException(plate, trafficTicketId)
    }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: ObjectId,
        violationIds: List<Int>
    ): MongoFine {
        mongoFineRepository.getFineByCarPlate(plate)
            ?: throw CarPlateNotFoundException(plate)
        return mongoFineRepository.addViolationToTrafficTicket(
            plate,
            trafficTicketId,
            getViolationsByIndexes(violationIds)
        ) ?: throw TrafficTicketNotFoundException(plate, trafficTicketId)
    }

    override fun removeViolationFromTicket(ticketId: ObjectId, violationId: Int): MongoFine =
        mongoFineRepository.removeViolationFromTicket(
            ticketId,
            Violation.entries[violationId].toMongoViolation().description
        ) ?: throw TrafficTicketWithViolationNotFoundException(ticketId, violationId)

    override fun getSumOfFinesForCarPlate(plate: String): TotalFineSumResponse {
        return mongoFineRepository.getSumOfFinesForCarPlate(plate) ?: TotalFineSumResponse(plate, 0.0)
    }

    override fun getAllCars() = mongoFineRepository.getAllCars().ifEmpty { throw CarsNotFoundException() }

    override fun updateCarById(
        fineId: ObjectId,
        @AutofillNullable(
            fieldToGenerate = "model",
            valueProvider = RandomModelGenerator::class
        )
        carRequest: CarRequest
    ): MongoFine =
        mongoFineRepository.updateCarById(fineId, carRequest.toMongoCar())
            ?: throw FineIdNotFoundException(fineId)
}
