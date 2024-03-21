package ua.anastasiia.finesapp.nats

import io.nats.client.Connection
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.infrastructure.mapper.toViolation
import ua.anastasiia.finesapp.infrastructure.mapper.toViolationType
import ua.anastasiia.finesapp.input.reqreply.violation.DeleteViolationRequest
import ua.anastasiia.finesapp.input.reqreply.violation.DeleteViolationResponse
import ua.anastasiia.finesapp.nats.NatsTestUtils.getFineToSave
import ua.anastasiia.finesapp.nats.NatsTestUtils.getTrafficTicketToSave
import ua.anastasiia.finesapp.nats.NatsTestUtils.sendRequestAndParseResponse
import ua.anastasiia.finesapp.output.pubsub.violation.ViolationDeletedEvent
import java.time.Duration

@SpringBootTest
@ActiveProfiles("test")
class DeleteViolationNatsControllerTest {

    @Autowired
    lateinit var connection: Connection

    @Autowired
    lateinit var fineRepository: FineRepositoryOutPort

    @Test
    fun `should update fine and publish related event when violation is deleted`() {
        // GIVEN
        val trafficTicketToSave = getTrafficTicketToSave()
        val fineToSave = getFineToSave().copy(trafficTickets = listOf(trafficTicketToSave))
        val savedFine = fineRepository.saveFine(fineToSave).block()
        val expectedTrafficTicket =
            trafficTicketToSave.copy(violations = listOf(1, 2).map { it.toViolationType().toViolation() })
        val expectedFine = savedFine!!.copy(trafficTickets = listOf(expectedTrafficTicket))
            .toProto()
        val createdEvent = connection.subscribe(NatsSubject.Violation.deletedSubject(savedFine.car.plate))
        val expectedResponse =
            DeleteViolationResponse.newBuilder().apply { successBuilder.setFine(expectedFine) }.build()
        val expectedEvent = ViolationDeletedEvent.newBuilder().setFine(expectedFine).build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Violation.DELETE,
            request = DeleteViolationRequest.newBuilder()
                .setCarPlate(fineToSave.car.plate)
                .setTicketId(savedFine.trafficTickets[0].id)
                .setViolationId(3)
                .build(),
            parser = DeleteViolationResponse::parseFrom
        )
        val actualEvent = ViolationDeletedEvent.parseFrom(createdEvent.nextMessage(Duration.ofSeconds(10)).data)

        // THEN
        assertEquals(expectedResponse, actualResponse)
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `should return failure response and not publish event when deleting non-existent or invalid violation`() {
        // GIVEN
        val invalidViolationId = 9
        val objectId = ObjectId()
        val ticketId = objectId.toHexString()
        val carPlate = "TE1234ST"
        val deleteViolationRequest = DeleteViolationRequest.newBuilder()
            .setCarPlate(carPlate)
            .setTicketId(ticketId)
            .setViolationId(invalidViolationId)
            .build()
        val expectedResponse = DeleteViolationResponse.newBuilder().apply {
            failureBuilder.trafficTicketWithViolationNotFoundErrorBuilder
        }.build()
        val createdEvent = connection.subscribe(NatsSubject.Violation.deletedSubject(carPlate))

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Violation.DELETE,
            request = deleteViolationRequest,
            parser = DeleteViolationResponse::parseFrom
        )
        val actualEvent = createdEvent.nextMessage(Duration.ofSeconds(2))

        // THEN
        assertTrue(actualResponse.hasFailure())
        assertEquals(expectedResponse, actualResponse)
        assertNull(actualEvent, "No event should be published for invalid violation deletion.")
    }
}
