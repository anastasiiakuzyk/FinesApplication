package ua.anastasiia.finesapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateResponse

class GetFineByCarPlateNatsControllerTest : NatsControllerTest() {

    @Test
    fun `verify fine retrieval by specific car plate`() {
        val savedFine = fineRepository.saveFine(getFineToSaveGeneratedCarPlate())

        val expectedResponse = GetFineByCarPlateResponse
            .newBuilder()
            .apply { successBuilder.setFine(savedFine.toProto()) }
            .build()

        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Fine.GET_BY_CAR_PLATE,
            request = GetFineByCarPlateRequest.newBuilder().setCarPlate(savedFine.toProto().car.plate).build(),
            parser = GetFineByCarPlateResponse::parseFrom
        )
        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `verify fine absent by specific car plate`() {
        val carPlate = "TE1234ST"

        val expectedResponse = GetFineByCarPlateResponse
            .newBuilder()
            .apply { failureBuilder.carPlateNotFoundErrorBuilder }
            .build()

        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Fine.GET_BY_CAR_PLATE,
            request = GetFineByCarPlateRequest.newBuilder().setCarPlate(carPlate).build(),
            parser = GetFineByCarPlateResponse::parseFrom
        )
        assertTrue(actualResponse.hasFailure())
        assertEquals(expectedResponse, actualResponse)
    }
}
