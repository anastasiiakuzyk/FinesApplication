package ua.anastasiia.finesapp.controller

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ua.anastasiia.finesapp.dto.TrafficTicketRequest
import ua.anastasiia.finesapp.entity.MongoFine
import ua.anastasiia.finesapp.service.FineService

@RestController
@RequestMapping(value = ["/tickets"])
class TrafficTicketController(val fineService: FineService) {
    @PutMapping("car/{carPlate}")
    fun addTrafficTicketByCarPlate(
        @PathVariable carPlate: String,
        @Valid @RequestBody ticketRequest: TrafficTicketRequest
    ): MongoFine = fineService.addTrafficTicketByCarPlate(carPlate, ticketRequest)

    @PatchMapping("car/{carPlate}/ticket/{ticketId}")
    fun updateTrafficTicketByCarPlateAndId(
        @PathVariable carPlate: String,
        @PathVariable ticketId: ObjectId,
        @Valid @RequestBody ticketRequest: TrafficTicketRequest
    ): MongoFine = fineService.updateTrafficTicketByCarPlateAndId(carPlate, ticketId, ticketRequest)
}
