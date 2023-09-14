package ua.anastasiia.finesapp.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ua.anastasiia.finesapp.dto.CarRequest
import ua.anastasiia.finesapp.dto.CarResponse
import ua.anastasiia.finesapp.service.CarService

@RestController
@RequestMapping(value = ["/cars"])
class CarController(val carService: CarService) {
    @GetMapping
    fun getAllCars(): List<CarResponse> = carService.getAllCars()

    @PostMapping
    fun newCar(@Valid @RequestBody car: CarRequest): CarResponse = carService.saveCar(car)

    @GetMapping("{plate}")
    fun getCar(@PathVariable plate: String): CarResponse = carService.getCarByPlate(plate)

    @PutMapping("{id}")
    fun updateCarByPlate(
        @Valid @RequestBody car: CarRequest,
        @PathVariable id: Long
    ): CarResponse = carService.updateCarById(car, id)
}
