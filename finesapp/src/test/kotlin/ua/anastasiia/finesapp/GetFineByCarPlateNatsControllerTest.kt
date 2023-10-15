package ua.anastasiia.finesapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateResponse

class GetFineByCarPlateNatsControllerTest : NatsControllerTest() {

    @Test
    fun testGetFineByCarPlate() {
        val savedFine = fineRepository.saveFine(getFineToSave())

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
}
