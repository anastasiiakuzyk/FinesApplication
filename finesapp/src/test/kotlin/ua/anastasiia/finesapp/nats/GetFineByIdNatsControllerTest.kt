package ua.anastasiia.finesapp.nats

import io.nats.client.Connection
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByIdResponse
import ua.anastasiia.finesapp.nats.NatsTestUtils.getFineToSave
import ua.anastasiia.finesapp.nats.NatsTestUtils.sendRequestAndParseResponse

@SpringBootTest
@ActiveProfiles("test")
class GetFineByIdNatsControllerTest {

    @Autowired
    lateinit var connection: Connection

    @Autowired
    lateinit var fineRepository: FineRepositoryOutPort

    @Test
    fun `should retrieve fine by a specific id`() {
        // GIVEN
        val savedFine = fineRepository.saveFine(getFineToSave()).block()
        val expectedResponse = GetFineByIdResponse
            .newBuilder()
            .apply { successBuilder.setFine(savedFine!!.toProto()) }
            .build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Fine.GET_BY_ID,
            request = GetFineByIdRequest.newBuilder().setId(savedFine!!.toProto().id).build(),
            parser = GetFineByIdResponse::parseFrom
        )

        // THEN
        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `should fail when fine is absent for a specific id`() {
        // GIVEN
        val id = ObjectId()
        val expectedResponse = GetFineByIdResponse
            .newBuilder()
            .apply { failureBuilder.fineIdNotFoundErrorBuilder }
            .build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Fine.GET_BY_ID,
            request = GetFineByIdRequest.newBuilder().setId(id.toHexString()).build(),
            parser = GetFineByIdResponse::parseFrom
        )

        // THEN
        assertTrue(actualResponse.hasFailure())
        assertEquals(expectedResponse, actualResponse)
    }
}
