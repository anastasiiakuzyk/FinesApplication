package ua.anastasiia.finesapp

import io.nats.client.Connection
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ua.anastasiia.finesapp.NatsTestUtils.getFineToSave
import ua.anastasiia.finesapp.NatsTestUtils.getTrafficTicketToSave
import ua.anastasiia.finesapp.NatsTestUtils.sendRequestAndParseResponse
import ua.anastasiia.finesapp.domain.toDomainFine
import ua.anastasiia.finesapp.domain.toDomainTrafficTicket
import ua.anastasiia.finesapp.domain.toMongoFine
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.trafficticket.AddTrafficTicketRequest
import ua.anastasiia.finesapp.input.reqreply.trafficticket.AddTrafficTicketResponse
import ua.anastasiia.finesapp.output.pubsub.trafficticket.TrafficTicketAddedEvent
import ua.anastasiia.finesapp.repository.MongoFineRepository
import java.time.Duration

@SpringBootTest
@ActiveProfiles("test")
class AddTrafficTicketNatsControllerTest {

    @Autowired
    lateinit var connection: Connection

    @Autowired
    lateinit var fineRepository: MongoFineRepository

    @Test
    fun `should add traffic ticket and publish event when valid request is given`() {
        // GIVEN
        val fineToSave = getFineToSave()
        val savedFine = fineRepository.saveFine(fineToSave.toDomainFine()).block()
        val trafficTicketToSave = getTrafficTicketToSave()
        val expectedFine = savedFine!!.copy(
            trafficTickets = listOf(trafficTicketToSave.toDomainTrafficTicket())
        ).toMongoFine().toProto()
        val createdEvent = connection.subscribe(
            NatsSubject.TrafficTicket.addedSubject(expectedFine.car.plate)
        )
        val expectedResponse = AddTrafficTicketResponse
            .newBuilder()
            .apply { successBuilder.setFine(expectedFine) }
            .build()
        val expectedEvent = TrafficTicketAddedEvent.newBuilder().setFine(expectedFine).build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.TrafficTicket.ADD,
            request = AddTrafficTicketRequest.newBuilder()
                .setCarPlate(fineToSave.car.plate)
                .setTrafficTicket(trafficTicketToSave.toProto())
                .build(),
            parser = AddTrafficTicketResponse::parseFrom
        )
        val actualEvent = TrafficTicketAddedEvent.parseFrom(createdEvent.nextMessage(Duration.ofSeconds(10)).data)

        // THEN
        assertEquals(expectedResponse, actualResponse)
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `should return failure result when adding invalid traffic ticket`() {
        // GIVEN
        val nonExistentFine = getFineToSave().copy(id = ObjectId())
        val invalidTrafficTicket = getTrafficTicketToSave()
        val request = AddTrafficTicketRequest.newBuilder()
            .setCarPlate(nonExistentFine.car.plate)
            .setTrafficTicket(invalidTrafficTicket.toProto())
            .build()
        val expectedResponse = AddTrafficTicketResponse.newBuilder()
            .apply { failureBuilder.carPlateNotFoundBuilder }
            .build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.TrafficTicket.ADD,
            request = request,
            parser = AddTrafficTicketResponse::parseFrom
        )

        // THEN
        assertEquals(expectedResponse, actualResponse)
    }
}
