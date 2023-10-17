package ua.anastasiia.finesapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.response.toCar
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsRequest
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsResponse

class GetAllCarsNatsControllerTest : NatsControllerTest() {

    @Test
    fun `verify retrieval of all cars with existing entries`() {
        fineRepository.saveFines(listOf(getFineToSaveGeneratedCarPlate()))

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
    fun `verify handling of no available cars scenario`() {
        val expectedResponse = GetAllCarsResponse
            .newBuilder()
            .apply { failureBuilder.carsNotFoundErrorBuilder }
            .build()

        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Car.GET_ALL,
            request = GetAllCarsRequest.newBuilder().build(),
            parser = GetAllCarsResponse::parseFrom
        )
        assertEquals(expectedResponse, actualResponse)
    }
}
