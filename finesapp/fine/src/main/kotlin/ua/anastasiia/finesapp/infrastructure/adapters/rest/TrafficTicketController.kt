package ua.anastasiia.finesapp.infrastructure.adapters.rest

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.application.port.input.FineServiceIn
import ua.anastasiia.finesapp.infrastructure.dto.request.TrafficTicketRequest
import ua.anastasiia.finesapp.infrastructure.dto.response.FineResponse

@RestController
@RequestMapping(value = ["/tickets"])
class TrafficTicketController(val fineService: FineServiceIn) {
    @PutMapping("car/{carPlate}")
    fun addTrafficTicketByCarPlate(
        @PathVariable carPlate: String,
        @Valid @RequestBody ticketRequest: TrafficTicketRequest
    ): Mono<FineResponse> = fineService.addTrafficTicketByCarPlate(carPlate, ticketRequest)

    @PatchMapping("car/{carPlate}/ticket/{ticketId}")
    fun updateTrafficTicketByCarPlateAndId(
        @PathVariable carPlate: String,
        @PathVariable ticketId: String,
        @Valid @RequestBody ticketRequest: TrafficTicketRequest
    ): Mono<FineResponse> = fineService.updateTrafficTicketByCarPlateAndId(carPlate, ticketId, ticketRequest)

    @DeleteMapping("car/{carPlate}/ticket/{ticketId}")
    fun removeViolationFromTicket(
        @PathVariable carPlate: String,
        @PathVariable ticketId: String
    ): Mono<FineResponse> = fineService.removeTicketByCarPlateAndId(carPlate, ticketId)
}
