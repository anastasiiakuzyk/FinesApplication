package ua.anastasiia.finesapp.service

import ua.anastasiia.finesapp.dto.FineRequest
import ua.anastasiia.finesapp.dto.FineResponse
import ua.anastasiia.finesapp.entity.Fine

interface FineService {

    fun createFine(fineRequest: FineRequest): Fine

    fun getAllFines(): List<FineResponse>

    fun getFinesByPlate(plate: String): List<FineResponse>

    fun addViolations(fineId: Long, violationIds: List<Long>): FineResponse

    fun getFineById(id: Long): FineResponse
}
