package ua.anastasiia.finesapp.service

import ua.anastasiia.finesapp.dto.CarRequest
import ua.anastasiia.finesapp.dto.CarResponse

interface CarService {
    fun saveCar(carRequest: CarRequest): CarResponse
    fun getCarByPlate(plate: String): CarResponse
    fun getCarById(id: Long): CarResponse
    fun getAllCars(): List<CarResponse>
    fun updateCarById(carUpdated: CarRequest, id: Long): CarResponse
}
