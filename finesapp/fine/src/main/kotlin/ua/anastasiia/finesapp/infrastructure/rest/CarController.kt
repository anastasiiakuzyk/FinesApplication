package ua.anastasiia.finesapp.infrastructure.rest

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.application.port.input.FineServiceInPort
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.rest.dto.request.CarRequest
import ua.anastasiia.finesapp.infrastructure.rest.dto.response.FineResponse
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toCar
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toResponse

@RestController
@RequestMapping(value = ["/cars"])
class CarController(val fineService: FineServiceInPort) {
    @GetMapping
    fun getAllCars(): Flux<Fine.Car> = fineService.getAllCars()

    @GetMapping("plate/{carPlate}")
    fun getFineByCarPlate(@PathVariable carPlate: String): Mono<FineResponse> =
        fineService.getFineByCarPlate(carPlate).map { it.toResponse() }

    @GetMapping("sum/car/{carPlate}")
    fun getSumOfFinesForCarPlate(
        @PathVariable carPlate: String
    ): Mono<Double> = fineService.getSumOfFinesForCarPlate(carPlate)

    @PutMapping("fine/{fineId}")
    fun updateCarById(
        @PathVariable fineId: String,
        @Valid @RequestBody carRequest: CarRequest
    ): Mono<FineResponse> = fineService.updateCarById(fineId, carRequest.toCar()).map { it.toResponse() }
}
