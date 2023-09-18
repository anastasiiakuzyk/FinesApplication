package ua.anastasiia.finesapp.service

import org.springframework.stereotype.Service
import ua.anastasiia.finesapp.annotation.NullableGenerate
import ua.anastasiia.finesapp.dto.CarRequest
import ua.anastasiia.finesapp.dto.CarResponse
import ua.anastasiia.finesapp.dto.toEntity
import ua.anastasiia.finesapp.dto.toResponse
import ua.anastasiia.finesapp.entity.Car
import ua.anastasiia.finesapp.exception.CarIdNotFoundException
import ua.anastasiia.finesapp.exception.CarPlateDuplicateException
import ua.anastasiia.finesapp.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.repository.CarRepository

@Service
class CarServiceImpl(val carRepository: CarRepository) : CarService {
    @NullableGenerate
    override fun saveCar(carRequest: CarRequest): CarResponse {
        return carRepository.findByPlate(carRequest.plate!!)?.let {
            throw CarPlateDuplicateException(carRequest.plate)
        } ?: carRepository.save(carRequest.toEntity()).toResponse()
    }

    override fun getCarByPlate(plate: String): CarResponse =
        carRepository.findByPlate(plate)?.toResponse()
            ?: throw CarPlateNotFoundException(plate)

    override fun getCarById(id: Long): CarResponse {
        return carRepository.findById(id).orElseThrow { throw CarIdNotFoundException(id) }.toResponse()
    }

    override fun getAllCars(): List<CarResponse> = carRepository.findAll().map { car: Car -> car.toResponse() }

    @NullableGenerate
    override fun updateCarById(carUpdated: CarRequest, id: Long): CarResponse {
        getCarById(id).copy(
            id = id,
            plate = carUpdated.plate!!,
            mark = carUpdated.mark,
            model = carUpdated.model,
            color = carUpdated.color
        ).also {
            carRepository.save(it.toEntity())
            return it.toEntity().toResponse()
        }
    }
}
