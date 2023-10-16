package ua.anastasiia.finesapp

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.dto.toViolation
import ua.anastasiia.finesapp.dto.toViolationType
import ua.anastasiia.finesapp.exception.TrafficTicketWithViolationNotFoundException
import ua.anastasiia.finesapp.input.reqreply.violation.DeleteViolationRequest
import ua.anastasiia.finesapp.input.reqreply.violation.DeleteViolationResponse
import ua.anastasiia.finesapp.output.pubsub.violation.ViolationDeletedEvent
import java.time.Duration

class DeleteViolationNatsControllerTest : NatsControllerTest() {

    @Test
    fun `verify violation deletion, fine update, and related event publication`() {
        val trafficTicketToSave = getTrafficTicketToSave()
        val fineToSave = getFineToSaveGeneratedCarPlate().copy(trafficTickets = listOf(trafficTicketToSave))
        val savedFine = fineRepository.saveFine(fineToSave)

        val expectedTrafficTicket =
            trafficTicketToSave.copy(violations = listOf(1, 2).map { it.toViolationType().toViolation() })
        val expectedFine = savedFine.copy(trafficTickets = listOf(expectedTrafficTicket)).toProto()

        val createdEvent = connection.subscribe(
            NatsSubject.Violation.eventSubject(savedFine.car.plate)
        )

        val expectedResponse = DeleteViolationResponse
            .newBuilder()
            .apply { successBuilder.setFine(expectedFine) }
            .build()

        val request = DeleteViolationRequest.newBuilder()
            .setCarPlate(fineToSave.car.plate)
            .setTicketId(savedFine.trafficTickets[0].id.toHexString())
            .setViolationId(3)
            .build()
        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Violation.DELETE,
            request = request,
            parser = DeleteViolationResponse::parseFrom
        )
        assertEquals(expectedResponse, actualResponse)

        val expectedEvent = ViolationDeletedEvent.newBuilder().setFine(expectedFine).build()
        val actualEvent = ViolationDeletedEvent.parseFrom(createdEvent.nextMessage(Duration.ofSeconds(10)).data)
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `verify failure when deleting non-existent or invalid violation`() {
        val invalidViolationId = 9
        val objectId = ObjectId()
        val ticketId = objectId.toHexString()
        val carPlate = "TE1234ST"
        val deleteViolationRequest = DeleteViolationRequest.newBuilder()
            .setCarPlate(carPlate)
            .setTicketId(ticketId)
            .setViolationId(invalidViolationId)
            .build()

        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Violation.DELETE,
            request = deleteViolationRequest,
            parser = DeleteViolationResponse::parseFrom
        )
        assertTrue(actualResponse.hasFailure())
        assertEquals(
            TrafficTicketWithViolationNotFoundException(objectId, invalidViolationId).message,
            actualResponse.failure.trafficTicketWithViolationNotFoundError.message
        )

        val createdEvent = connection.subscribe(NatsSubject.Violation.eventSubject(carPlate))
        val event = createdEvent.nextMessage(Duration.ofSeconds(2))
        assertNull(event, "No event should be published for invalid violation deletion.")
    }
}
