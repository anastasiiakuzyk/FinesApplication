package ua.anastasiia.finesapp.controller

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ua.anastasiia.finesapp.dto.FineRequest
import ua.anastasiia.finesapp.entity.MongoFine
import ua.anastasiia.finesapp.service.FineService
import java.time.LocalDate

@RestController
@RequestMapping(value = ["/fines"])
class FineController(val fineService: FineService) {

    @GetMapping
    fun getAllFines(): List<MongoFine> = fineService.getAllFines()

    @GetMapping("location")
    fun getAllFinesInLocation(
        @RequestParam longitude: Double,
        @RequestParam latitude: Double,
        @RequestParam radius: Double
    ): List<MongoFine> =
        fineService.getAllFinesInLocation(longitude, latitude, radius)

    @GetMapping("date/{date}")
    fun getAllFinesByDate(@PathVariable date: LocalDate): List<MongoFine> =
        fineService.getAllFinesByDate(date)

    @GetMapping("fine/{fineId}")
    fun getFineById(@PathVariable fineId: ObjectId): MongoFine =
        fineService.getFineById(fineId)

    @PostMapping
    fun saveFine(@Valid @RequestBody fineRequest: FineRequest): MongoFine = fineService.saveFine(fineRequest)

    @PostMapping("many")
    fun saveFines(@Valid @RequestBody mongoFines: List<FineRequest>): List<MongoFine> =
        fineService.saveFines(mongoFines)

    @DeleteMapping("fine/{fineId}")
    fun deleteFineById(@PathVariable fineId: ObjectId): MongoFine = fineService.deleteFineById(fineId)
}
