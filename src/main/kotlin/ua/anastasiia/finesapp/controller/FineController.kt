package ua.anastasiia.finesapp.controller

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ua.anastasiia.finesapp.dto.CarRequest
import ua.anastasiia.finesapp.dto.FineRequest
import ua.anastasiia.finesapp.dto.TotalFineSumResponse
import ua.anastasiia.finesapp.dto.TrafficTicketRequest
import ua.anastasiia.finesapp.entity.MongoFine
import ua.anastasiia.finesapp.service.MongoFineService
import java.time.LocalDate

@RestController
@RequestMapping(value = ["/fines"])
@Suppress("TooManyFunctions")
class FineController(val mongoFineService: MongoFineService) {

    @GetMapping
    fun getAllFines(): List<MongoFine> = mongoFineService.getAllFines()

    @GetMapping("location")
    fun getAllFinesInLocation(@RequestParam longitude: Double, @RequestParam latitude: Double): List<MongoFine> =
        mongoFineService.getAllFinesInLocation(longitude, latitude)

    @GetMapping("date/{date}")
    fun getAllFinesInLocation(@PathVariable date: LocalDate): List<MongoFine> =
        mongoFineService.getAllFinesByDate(date)

    @GetMapping("id/{id}")
    fun getFineById(@PathVariable id: ObjectId): MongoFine =
        mongoFineService.getFineById(id)

    @GetMapping("plate/{plate}")
    fun getFineByCarPlate(@PathVariable plate: String): MongoFine =
        mongoFineService.getFineByCarPlate(plate)

    @PostMapping
    fun saveFine(@Valid @RequestBody fineRequest: FineRequest): MongoFine = mongoFineService.saveFine(fineRequest)

    @PostMapping("all")
    fun saveFines(@Valid @RequestBody mongoFines: List<FineRequest>): List<MongoFine> =
        mongoFineService.saveFines(mongoFines)

    @DeleteMapping("{id}")
    fun deleteFineById(@PathVariable id: ObjectId): MongoFine = mongoFineService.deleteFineById(id)

    @PatchMapping("add_ticket/{carPlate}")
    fun addTrafficTicketByCarPlate(
        @PathVariable carPlate: String,
        @Valid @RequestBody ticketRequest: TrafficTicketRequest
    ): MongoFine = mongoFineService.addTrafficTicketByCarPlate(carPlate, ticketRequest)

    @PatchMapping("update_ticket/{carPlate}/{ticketId}")
    fun updateTrafficTicketByCarPlateAndId(
        @PathVariable carPlate: String,
        @PathVariable ticketId: ObjectId,
        @Valid @RequestBody ticketRequest: TrafficTicketRequest
    ): MongoFine = mongoFineService.updateTrafficTicketByCarPlateAndId(carPlate, ticketId, ticketRequest)

    @PatchMapping("add_violation/{carPlate}/{ticketId}/{violationIds}")
    fun addViolationToTrafficTicket(
        @PathVariable carPlate: String,
        @PathVariable ticketId: ObjectId,
        @PathVariable violationIds: List<Int>
    ): MongoFine = mongoFineService.addViolationToTrafficTicket(carPlate, ticketId, violationIds)

    @DeleteMapping("remove_violation/{ticketId}/{violationId}")
    fun removeViolationFromTicket(
        @PathVariable ticketId: ObjectId,
        @PathVariable violationId: Int
    ): MongoFine = mongoFineService.removeViolationFromTicket(ticketId, violationId)

    @GetMapping("sum/{carPlate}")
    fun getSumOfFinesForCarPlate(
        @PathVariable carPlate: String
    ): TotalFineSumResponse = mongoFineService.getSumOfFinesForCarPlate(carPlate)

    @GetMapping("cars")
    fun getAllCars() = mongoFineService.getAllCars()

    @PutMapping("cars/{fineId}")
    fun updateCarById(
        @PathVariable fineId: ObjectId,
        @Valid @RequestBody carRequest: CarRequest
    ): MongoFine = mongoFineService.updateCarById(fineId, carRequest)
}
