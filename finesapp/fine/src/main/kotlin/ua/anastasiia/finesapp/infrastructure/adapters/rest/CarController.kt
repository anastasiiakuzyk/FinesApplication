package ua.anastasiia.finesapp.infrastructure.adapters.rest

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.infrastructure.dto.request.CarRequest
import ua.anastasiia.finesapp.infrastructure.dto.response.FineResponse
import ua.anastasiia.finesapp.infrastructure.dto.response.TotalFineSumResponse
import ua.anastasiia.finesapp.application.port.input.FineServiceIn

@RestController
@RequestMapping(value = ["/cars"])
class CarController(val fineService: FineServiceIn) {
    @GetMapping
    fun getAllCars() = fineService.getAllCars()

    @GetMapping("plate/{carPlate}")
    fun getFineByCarPlate(@PathVariable carPlate: String): Mono<FineResponse> =
        fineService.getFineByCarPlate(carPlate)

    @GetMapping("sum/car/{carPlate}")
    fun getSumOfFinesForCarPlate(
        @PathVariable carPlate: String
    ): Mono<TotalFineSumResponse> = fineService.getSumOfFinesForCarPlate(carPlate)

    @PutMapping("fine/{fineId}")
    fun updateCarById(
        @PathVariable fineId: String,
        @Valid @RequestBody carRequest: CarRequest
    ): Mono<FineResponse> = fineService.updateCarById(fineId, carRequest)
}
