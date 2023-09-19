package ua.anastasiia.finesapp.service

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
class FineServiceImpl(
    val fineRepository: FineRepository,
    val carService: CarService,
    val violationRepository: ViolationRepository
) : FineService {
    override fun createFine(fineRequest: FineRequest): Fine {
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

    override fun getAllFines(): List<FineResponse> = fineRepository.findAll().map { it.toResponse() }
    
    override fun getFinesByPlate(plate: String): List<FineResponse> =
        fineRepository.findAllByCarPlate(plate).map { it.toResponse() }
            .ifEmpty { throw CarPlateNotFoundException(plate) }

    override fun addViolations(fineId: Long, violationIds: Array<Long>): FineResponse {
        val fine = fineRepository.findById(fineId).orElseThrow { FineIdNotFoundException(fineId) }
        fine.violations += (violationIds - fine.violations).asSequence()
                     .distinct()
                     .map { 
                         violationRepository.findById(violationId).orElseThrow { FineIdNotFoundException(fineId) }
                     }
                     .toList()
        fineRepository.save(fine)
        return fine.toResponse()
    }

    override fun getFineById(id: Long): FineResponse {
        return fineRepository.findById(id).orElseThrow { throw CarIdNotFoundException(id) }.toResponse()
    }
}
