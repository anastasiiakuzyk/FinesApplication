package ua.anastasiia.finesapp.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ua.anastasiia.finesapp.dto.FineRequest
import ua.anastasiia.finesapp.dto.FineResponse
import ua.anastasiia.finesapp.entity.Fine
import ua.anastasiia.finesapp.service.FineService

@RestController
@RequestMapping(value = ["/fines"])
class FineController(val fineService: FineService) {

    @GetMapping
    fun getAllFines(): List<FineResponse> = fineService.getAllFines()

    @PostMapping
    fun newFine(@RequestBody fineRequest: FineRequest): Fine = fineService.createFine(fineRequest)

    @GetMapping("{id}")
    fun getFine(@PathVariable id: Long): FineResponse = fineService.getFineById(id)

    @GetMapping("{plate}")
    fun getFinesByPlate(@PathVariable plate: String): List<FineResponse> = fineService.getFinesByPlate(plate)

    @PatchMapping("{fineId}/violations/{violationIds}")
    fun addViolation(@PathVariable fineId: Long, @PathVariable violationIds: Array<Long>): FineResponse =
        fineService.addViolations(fineId, violationIds)
}
