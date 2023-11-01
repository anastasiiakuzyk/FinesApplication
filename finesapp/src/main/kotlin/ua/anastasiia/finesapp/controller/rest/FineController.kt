package ua.anastasiia.finesapp.controller.rest

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.dto.request.FineRequest
import ua.anastasiia.finesapp.dto.response.FineResponse
import ua.anastasiia.finesapp.service.FineService
import java.time.LocalDate

@RestController
@RequestMapping(value = ["/fines"])
class FineController(val fineService: FineService) {

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getAllFines(): Flux<FineResponse> = fineService.getAllFines()

    @GetMapping("location")
    fun getAllFinesInLocation(
        @RequestParam longitude: Double,
        @RequestParam latitude: Double,
        @RequestParam radius: Double
    ): Flux<FineResponse> =
        fineService.getAllFinesInLocation(longitude, latitude, radius)

    @GetMapping("date/{date}")
    fun getAllFinesByDate(@PathVariable date: LocalDate): Flux<FineResponse> =
        fineService.getAllFinesByDate(date)

    @GetMapping("fine/{fineId}")
    fun getFineById(@PathVariable fineId: ObjectId): Mono<FineResponse> =
        fineService.getFineById(fineId)

    @PostMapping
    fun saveFine(@Valid @RequestBody fineRequest: FineRequest): Mono<FineResponse> = fineService.saveFine(fineRequest)

    @PostMapping("many")
    fun saveFines(@Valid @RequestBody mongoFines: List<FineRequest>): Flux<FineResponse> =
        fineService.saveFines(mongoFines)

    @DeleteMapping("fine/{fineId}")
    fun deleteFineById(@PathVariable fineId: ObjectId): Mono<FineResponse> = fineService.deleteFineById(fineId)
}
