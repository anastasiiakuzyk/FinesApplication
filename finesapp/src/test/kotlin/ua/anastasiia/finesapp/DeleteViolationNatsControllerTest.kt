package ua.anastasiia.finesapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.dto.toViolation
import ua.anastasiia.finesapp.dto.toViolationType
import ua.anastasiia.finesapp.input.reqreply.violation.DeleteViolationRequest
import ua.anastasiia.finesapp.input.reqreply.violation.DeleteViolationResponse
import ua.anastasiia.finesapp.output.pubsub.violation.ViolationDeletedEvent
import java.time.Duration

class DeleteViolationNatsControllerTest : NatsControllerTest() {

    @Test
    fun testAddedTrafficTicket() {
        val trafficTicketToSave = getTrafficTicketToSave()
        val fineToSave = getFineToSave().copy(trafficTickets = listOf(trafficTicketToSave))
        val savedFine = fineRepository.saveFine(fineToSave)

        val expectedTrafficTicket =
            trafficTicketToSave.copy(violations = listOf(1, 2).map { it.toViolationType().toViolation() })
        val expectedFine = savedFine.copy(trafficTickets = listOf(expectedTrafficTicket)).toProto()

        val createdEvent = connection.subscribe(
            NatsSubject.Violation.getDeletedEventSubject(savedFine.car.plate)
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
}
