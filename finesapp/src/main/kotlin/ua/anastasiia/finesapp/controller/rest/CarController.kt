package ua.anastasiia.finesapp.controller.rest

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ua.anastasiia.finesapp.dto.request.CarRequest
import ua.anastasiia.finesapp.dto.response.FineResponse
import ua.anastasiia.finesapp.dto.response.TotalFineSumResponse
import ua.anastasiia.finesapp.entity.MongoFine
import ua.anastasiia.finesapp.service.FineService

@RestController
@RequestMapping(value = ["/cars"])
class CarController(val fineService: FineService) {
    @GetMapping
    fun getAllCars() = fineService.getAllCars()

    @GetMapping("plate/{carPlate}")
    fun getFineByCarPlate(@PathVariable carPlate: String): FineResponse =
        fineService.getFineByCarPlate(carPlate)

    @GetMapping("sum/car/{carPlate}")
    fun getSumOfFinesForCarPlate(
        @PathVariable carPlate: String
    ): TotalFineSumResponse = fineService.getSumOfFinesForCarPlate(carPlate)

    @PutMapping("fine/{fineId}")
    fun updateCarById(
        @PathVariable fineId: ObjectId,
        @Valid @RequestBody carRequest: CarRequest
    ): FineResponse = fineService.updateCarById(fineId, carRequest)
}
