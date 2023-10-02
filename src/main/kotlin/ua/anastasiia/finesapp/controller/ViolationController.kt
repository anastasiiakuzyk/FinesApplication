package ua.anastasiia.finesapp.controller

import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ua.anastasiia.finesapp.entity.MongoFine
import ua.anastasiia.finesapp.service.FineService

@RestController
@RequestMapping(value = ["/violations"])
class ViolationController(val fineService: FineService) {
    @PatchMapping("car/{carPlate}/ticket/{ticketId}/violations/{violationIds}")
    fun addViolationToTrafficTicket(
        @PathVariable carPlate: String,
        @PathVariable ticketId: ObjectId,
        @PathVariable violationIds: List<Int>
    ): MongoFine = fineService.addViolationToTrafficTicket(carPlate, ticketId, violationIds)

    @DeleteMapping("car/{carPlate}/ticket/{ticketId}/violation/{violationId}")
    fun removeViolationFromTicket(
        @PathVariable carPlate: String,
        @PathVariable ticketId: ObjectId,
        @PathVariable violationId: Int
    ): MongoFine = fineService.removeViolationFromTicket(carPlate, ticketId, violationId)
}
