package ua.anastasiia.finesapp

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdResponse

class GetFineByIdNatsControllerTest : NatsControllerTest() {

    @Test
    fun `verify fine retrieval by specific id`() {
        val savedFine = fineRepository.saveFine(getFineToSaveGeneratedCarPlate())

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

    @Test
    fun `verify fine absent by specific id`() {
        val id = ObjectId()

        val expectedResponse = GetFineByIdResponse
            .newBuilder()
            .apply { failureBuilder.fineIdNotFoundErrorBuilder }
            .build()

        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Fine.GET_BY_ID,
            request = GetFineByIdRequest.newBuilder().setId(id.toHexString()).build(),
            parser = GetFineByIdResponse::parseFrom
        )
        assertTrue(actualResponse.hasFailure())
        assertEquals(expectedResponse, actualResponse)
    }
}
