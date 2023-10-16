package ua.anastasiia.finesapp.service

import org.bson.types.ObjectId
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import ua.anastasiia.finesapp.annotation.AutofillNullable
import ua.anastasiia.finesapp.annotation.NullableGenerate
import ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration.RandomModelGenerator
import ua.anastasiia.finesapp.dto.request.CarRequest
import ua.anastasiia.finesapp.dto.request.FineRequest
import ua.anastasiia.finesapp.dto.request.TrafficTicketRequest
import ua.anastasiia.finesapp.dto.request.toCar
import ua.anastasiia.finesapp.dto.request.toFine
import ua.anastasiia.finesapp.dto.request.toTrafficTicket
import ua.anastasiia.finesapp.dto.response.CarResponse
import ua.anastasiia.finesapp.dto.response.FineResponse
import ua.anastasiia.finesapp.dto.response.TotalFineSumResponse
import ua.anastasiia.finesapp.dto.response.toResponse
import ua.anastasiia.finesapp.dto.toViolation
import ua.anastasiia.finesapp.dto.toViolationType
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
class FineServiceImpl(val mongoFineRepository: MongoFineRepository) : FineService {
    override fun getAllFines(): List<FineResponse> =
        mongoFineRepository.getAllFines().map { it.toResponse() }.ifEmpty { throw NoFinesFoundException() }

    override fun getAllFinesInLocation(
        longitude: Double,
        latitude: Double,
        radiusInMeters: Double
    ): List<FineResponse> =
        mongoFineRepository.getAllFinesInLocation(longitude, latitude, radiusInMeters).map { it.toResponse() }
            .ifEmpty { throw FinesInLocationNotFound(longitude, latitude) }

    override fun getAllFinesByDate(date: LocalDate): List<FineResponse> =
        mongoFineRepository.getAllFinesByDate(date).map { it.toResponse() }
            .ifEmpty { throw NoFinesFoundByDateException(date) }

    override fun getFineById(fineId: ObjectId): FineResponse =
        mongoFineRepository.getFineById(fineId)?.toResponse() ?: throw FineIdNotFoundException(fineId)

    override fun getFineByCarPlate(plate: String): FineResponse =
        mongoFineRepository.getFineByCarPlate(plate)?.toResponse() ?: throw CarPlateNotFoundException(plate)

    override fun saveFine(fineRequest: FineRequest): FineResponse =
        runCatching {
            mongoFineRepository.getFineByCarPlate(fineRequest.car.plate)?.let {
                throw CarPlateDuplicateException(fineRequest.car.plate)
            }
            mongoFineRepository.saveFine(fineRequest.toFine()).toResponse()
        }.getOrElse { exception ->
            when (exception) {
                is DuplicateKeyException -> throw CarPlateDuplicateException(fineRequest.car.plate)
                else -> throw exception
            }
        }

    override fun saveFines(mongoFines: List<FineRequest>): List<FineResponse> =
        runCatching {
            mongoFineRepository.saveFines(mongoFines.map { it.toFine() }).map { it.toResponse() }
        }.getOrElse { exception ->
            when (exception) {
                is DuplicateKeyException -> throw CarPlateDuplicateException(mongoFines.joinToString { it.car.plate })
                else -> throw exception
            }
        }

    override fun deleteFineById(fineId: ObjectId): FineResponse =
        mongoFineRepository.deleteFineById(fineId)?.toResponse() ?: throw FineIdNotFoundException(fineId)

    override fun addTrafficTicketByCarPlate(plate: String, ticketRequest: TrafficTicketRequest): FineResponse =
        mongoFineRepository.getFineByCarPlate(plate)?.let {
            mongoFineRepository.addTrafficTicketByCarPlate(plate, ticketRequest.toTrafficTicket())?.toResponse()
        } ?: throw CarPlateNotFoundException(plate)

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicketRequest: TrafficTicketRequest
    ): FineResponse {
        mongoFineRepository.getFineByCarPlate(plate)
            ?: throw CarPlateNotFoundException(plate)
        return mongoFineRepository.updateTrafficTicketByCarPlateAndId(
            plate,
            trafficTicketId,
            updatedTicketRequest.copy(id = trafficTicketId).toTrafficTicket()
        )?.toResponse() ?: throw TrafficTicketNotFoundException(plate, trafficTicketId)
    }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: ObjectId,
        violationIds: List<Int>
    ): FineResponse {
        mongoFineRepository.getFineByCarPlate(plate)
            ?: throw CarPlateNotFoundException(plate)
        return mongoFineRepository.addViolationToTrafficTicket(
            plate,
            trafficTicketId,
            violationIds.map { violationId ->
                violationId.toViolationType().toViolation()
            }
        )?.toResponse() ?: throw TrafficTicketNotFoundException(plate, trafficTicketId)
    }

    override fun removeViolationFromTicket(carPlate: String, ticketId: ObjectId, violationId: Int): FineResponse =
        mongoFineRepository.removeViolationFromTicket(
            carPlate,
            ticketId,
            violationId.toViolationType().toViolation().description
        )?.toResponse() ?: throw TrafficTicketWithViolationNotFoundException(ticketId, violationId)

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): FineResponse =
        mongoFineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)?.toResponse()
            ?: throw TrafficTicketNotFoundException(carPlate, ticketId)

    override fun getSumOfFinesForCarPlate(plate: String): TotalFineSumResponse =
        mongoFineRepository.getSumOfFinesForCarPlate(plate) ?: TotalFineSumResponse(plate, 0.0)

    override fun getAllCars(): List<CarResponse> =
        mongoFineRepository.getAllCars().ifEmpty { throw CarsNotFoundException() }

    override fun updateCarById(
        fineId: ObjectId,
        @AutofillNullable(
            fieldToGenerate = "model",
            valueProvider = RandomModelGenerator::class
        )
        carRequest: CarRequest
    ): FineResponse =
        mongoFineRepository.updateCarById(fineId, carRequest.toCar())?.toResponse()
            ?: throw FineIdNotFoundException(fineId)
}
