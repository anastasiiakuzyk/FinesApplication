package ua.anastasiia.finesapp.infrastructure.rest

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.application.port.input.FineServiceInPort
import ua.anastasiia.finesapp.infrastructure.rest.dto.request.TrafficTicketRequest
import ua.anastasiia.finesapp.infrastructure.rest.dto.response.FineResponse
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toResponse
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toTrafficTicket

@RestController
@RequestMapping(value = ["/tickets"])
class TrafficTicketController(val fineService: FineServiceInPort) {

    @PutMapping("car/{carPlate}")
    fun addTrafficTicketByCarPlate(
        @PathVariable carPlate: String,
        @Valid @RequestBody ticketRequest: TrafficTicketRequest,
    ): Mono<FineResponse> =
        fineService.addTrafficTicketByCarPlate(carPlate, ticketRequest.toTrafficTicket()).map { it.toResponse() }

    @PatchMapping("car/{carPlate}/ticket/{ticketId}")
    fun updateTrafficTicketByCarPlateAndId(
        @PathVariable carPlate: String,
        @PathVariable ticketId: String,
        @Valid @RequestBody ticketRequest: TrafficTicketRequest,
    ): Mono<FineResponse> =
        fineService.updateTrafficTicketByCarPlateAndId(carPlate, ticketId, ticketRequest.toTrafficTicket())
            .map { it.toResponse() }

    @DeleteMapping("car/{carPlate}/ticket/{ticketId}")
    fun deleteTrafficTicketByCarPlateAndId(
        @PathVariable carPlate: String,
        @PathVariable ticketId: String,
    ): Mono<FineResponse> = fineService.deleteTrafficTicketByCarPlateAndId(carPlate, ticketId).map { it.toResponse() }
}
