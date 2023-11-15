package ua.anastasiia.finesapp.infrastructure.adapters.rest

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.infrastructure.dto.response.FineResponse
import ua.anastasiia.finesapp.application.port.input.FineServiceIn

@RestController
@RequestMapping(value = ["/violations"])
class ViolationController(val fineService: FineServiceIn) {
    @PatchMapping("car/{carPlate}/ticket/{ticketId}/violations/{violationIds}")
    fun addViolationToTrafficTicket(
        @PathVariable carPlate: String,
        @PathVariable ticketId: String,
        @PathVariable violationIds: List<Int>
    ): Mono<FineResponse> = fineService.addViolationToTrafficTicket(carPlate, ticketId, violationIds)

    @DeleteMapping("car/{carPlate}/ticket/{ticketId}/violation/{violationId}")
    fun removeViolationFromTicket(
        @PathVariable carPlate: String,
        @PathVariable ticketId: String,
        @PathVariable violationId: Int
    ): Mono<FineResponse> = fineService.removeViolationFromTicket(carPlate, ticketId, violationId)
}
