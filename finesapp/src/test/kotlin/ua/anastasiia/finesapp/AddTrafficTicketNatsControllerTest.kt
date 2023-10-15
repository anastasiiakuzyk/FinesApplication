package ua.anastasiia.finesapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.trafficticket.AddTrafficTicketRequest
import ua.anastasiia.finesapp.input.reqreply.trafficticket.AddTrafficTicketResponse
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent
import java.time.Duration

class AddTrafficTicketNatsControllerTest : NatsControllerTest() {

    @Test
    fun testAddedTrafficTicket() {
        val fineToSave = getFineToSave()
        val savedFine = fineRepository.saveFine(fineToSave)
        val trafficTicketToSave = getTrafficTicketToSave()
        val expectedFine = savedFine.copy(trafficTickets = listOf(trafficTicketToSave)).toProto()

        val createdEvent = connection.subscribe(
            NatsSubject.TrafficTicket.getAddedEventSubject(expectedFine.car.plate)
        )

        val expectedResponse = AddTrafficTicketResponse
            .newBuilder()
            .apply { successBuilder.setFine(expectedFine) }
            .build()

        val request = AddTrafficTicketRequest.newBuilder()
            .setCarPlate(fineToSave.car.plate)
            .setTrafficTicket(trafficTicketToSave.toProto())
            .build()
        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.TrafficTicket.ADD,
            request = request,
            parser = AddTrafficTicketResponse::parseFrom
        )
        assertEquals(expectedResponse, actualResponse)

        val expectedEvent = TrafficTicketAddedEvent.newBuilder().setFine(expectedFine).build()
        val actualEvent = TrafficTicketAddedEvent.parseFrom(createdEvent.nextMessage(Duration.ofSeconds(10)).data)
        assertEquals(expectedEvent, actualEvent)
    }
}
