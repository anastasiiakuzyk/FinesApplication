package ua.anastasiia.finesapp.infrastructure.rest

import jakarta.validation.Valid
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
import ua.anastasiia.finesapp.application.port.input.FineServiceInPort
import ua.anastasiia.finesapp.infrastructure.rest.dto.request.FineRequest
import ua.anastasiia.finesapp.infrastructure.rest.dto.response.FineResponse
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toFine
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toResponse
import java.time.LocalDate

@RestController
@RequestMapping(value = ["/fines"])
class FineController(val fineService: FineServiceInPort) {

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getAllFines(): Flux<FineResponse> = fineService.getAllFines().map { it.toResponse() }

    @GetMapping("location")
    fun getAllFinesInLocation(
        @RequestParam longitude: Double,
        @RequestParam latitude: Double,
        @RequestParam radius: Double
    ): Flux<FineResponse> =
        fineService.getAllFinesInLocation(longitude, latitude, radius).map { it.toResponse() }

    @GetMapping("date/{date}")
    fun getAllFinesByDate(@PathVariable date: LocalDate): Flux<FineResponse> =
        fineService.getAllFinesByDate(date).map { it.toResponse() }

    @GetMapping("fine/{fineId}")
    fun getFineById(@PathVariable fineId: String): Mono<FineResponse> =
        fineService.getFineById(fineId).map { it.toResponse() }

    @PostMapping
    fun saveFine(@Valid @RequestBody fineRequest: FineRequest): Mono<FineResponse> =
        fineService.saveFine(fineRequest.toFine()).map { it.toResponse() }

    @PostMapping("many")
    fun saveFines(@Valid @RequestBody mongoFines: List<FineRequest>): Flux<FineResponse> =
        fineService.saveFines(mongoFines.map { it.toFine() }).map { it.toResponse() }

    @DeleteMapping("fine/{fineId}")
    fun deleteFineById(@PathVariable fineId: String): Mono<FineResponse> =
        fineService.deleteFineById(fineId).map { it.toResponse() }
}
