package ua.anastasiia.finesapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.response.toCar
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.exception.CarsNotFoundException
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsRequest
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsResponse

class GetAllCarsNatsControllerTest : NatsControllerTest() {

    @Test
    fun testGetAll() {
        fineRepository.saveFines(listOf(getFineToSave()))

        val expectedResponse = GetAllCarsResponse
            .newBuilder()
            .apply { successBuilder.addAllCars(fineRepository.getAllCars().map { it.toCar().toProto() }) }
            .build()

        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Car.GET_ALL,
            request = GetAllCarsRequest.newBuilder().build(),
            parser = GetAllCarsResponse::parseFrom
        )
        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun testGetAllEmpty() {
        clean()
        val expectedResponse = GetAllCarsResponse
            .newBuilder()
            .apply { failureBuilder.setMessage(CarsNotFoundException().message) }
            .build()

        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Car.GET_ALL,
            request = GetAllCarsRequest.newBuilder().build(),
            parser = GetAllCarsResponse::parseFrom
        )
        assertEquals(expectedResponse, actualResponse)
    }
}
