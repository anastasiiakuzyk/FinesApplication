package ua.anastasiia.finesapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdResponse

class GetFineByIdNatsControllerTest : NatsControllerTest() {

    @Test
    fun testGetFineById() {
        val savedFine = fineRepository.saveFine(getFineToSave())

        val expectedResponse = GetFineByIdResponse
            .newBuilder()
            .apply { successBuilder.setFine(savedFine.toProto()) }
            .build()

        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Fine.GET_BY_ID,
            request = GetFineByIdRequest.newBuilder().setId(savedFine.toProto().id).build(),
            parser = GetFineByIdResponse::parseFrom
        )
        assertEquals(expectedResponse, actualResponse)
    }
}
