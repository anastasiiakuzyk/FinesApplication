package ua.anastasiia.finesapp.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import ua.anastasiia.finesapp.dto.FineRequest
import ua.anastasiia.finesapp.dto.FineResponse
import ua.anastasiia.finesapp.dto.toEntity
import ua.anastasiia.finesapp.dto.toResponse
import ua.anastasiia.finesapp.entity.Fine
import ua.anastasiia.finesapp.exception.CarIdNotFoundException
import ua.anastasiia.finesapp.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.exception.FineIdNotFoundException
import ua.anastasiia.finesapp.exception.ViolationNotFoundException
import ua.anastasiia.finesapp.repository.FineRepository
import ua.anastasiia.finesapp.repository.ViolationRepository

@Service
class FineService(
    val fineRepository: FineRepository,
    val carService: CarService,
    val violationRepository: ViolationRepository
) {
    @Transactional
    fun createFine(fineRequest: FineRequest): Fine {
        val car = carService.getCarByPlate(fineRequest.plate)
        val fine = Fine(
            longitude = fineRequest.longitude,
            latitude = fineRequest.latitude,
            dateTime = fineRequest.dateTime,
            photoUrl = fineRequest.photoUrl,
            car = car.toEntity()
        )
        return fineRepository.save(fine)
    }

    fun getAllFines(): List<FineResponse> = fineRepository.findAll().map { it.toResponse() }
    fun getFinesByPlate(plate: String): List<FineResponse> =
        fineRepository.findByCarPlate(plate).map { it.toResponse() }.ifEmpty { throw CarPlateNotFoundException(plate) }

    fun addViolations(fineId: Long, violationIds: Array<Long>): FineResponse {
        val fine = fineRepository.findById(fineId).orElseThrow { FineIdNotFoundException(fineId) }
        violationIds.toSet().map { violationId ->
            violationRepository.findById(violationId).orElseThrow { ViolationNotFoundException(violationId) }
        }.filter { violation -> !fine.violations.contains(violation) }
            .forEach { violation -> fine.violations.add(violation) }
        fineRepository.save(fine)
        return fine.toResponse()
    }

    fun getFineById(id: Long): FineResponse {
        return fineRepository.findById(id).orElseThrow { throw CarIdNotFoundException(id) }.toResponse()
    }
}
