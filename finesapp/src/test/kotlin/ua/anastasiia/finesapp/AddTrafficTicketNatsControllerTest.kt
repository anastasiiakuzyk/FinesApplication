package ua.anastasiia.finesapp

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.input.reqreply.trafficticket.AddTrafficTicketRequest
import ua.anastasiia.finesapp.input.reqreply.trafficticket.AddTrafficTicketResponse
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent
import java.time.Duration

class AddTrafficTicketNatsControllerTest : NatsControllerTest() {

    @Test
    fun `verify traffic ticket addition and related event publication`() {
        val fineToSave = getFineToSaveGeneratedCarPlate()
        val savedFine = fineRepository.saveFine(fineToSave)
        val trafficTicketToSave = getTrafficTicketToSave()
        val expectedFine = savedFine.copy(trafficTickets = listOf(trafficTicketToSave)).toProto()

        val createdEvent = connection.subscribe(
            NatsSubject.TrafficTicket.eventSubject(expectedFine.car.plate)
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

    @Test
    fun `verify failure when adding invalid traffic ticket`() {
        val nonExistentFine = getFineToSaveGeneratedCarPlate().copy(id = ObjectId())
        val invalidTrafficTicket = getTrafficTicketToSave()

        val request = AddTrafficTicketRequest.newBuilder()
            .setCarPlate(nonExistentFine.car.plate)
            .setTrafficTicket(invalidTrafficTicket.toProto())
            .build()
        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.TrafficTicket.ADD,
            request = request,
            parser = AddTrafficTicketResponse::parseFrom
        )

        val expectedResponse = AddTrafficTicketResponse.newBuilder()
            .apply {
                failureBuilder.apply {
                    carPlateNotFoundBuilder.setMessage(CarPlateNotFoundException(nonExistentFine.car.plate).message)
                }
            }
            .build()
        assertEquals(expectedResponse, actualResponse)
    }
}
