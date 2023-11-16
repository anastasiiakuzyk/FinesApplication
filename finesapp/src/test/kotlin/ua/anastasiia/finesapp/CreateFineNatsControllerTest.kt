package ua.anastasiia.finesapp

import io.nats.client.Connection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ua.anastasiia.finesapp.NatsTestUtils.getFineToSave
import ua.anastasiia.finesapp.NatsTestUtils.sendRequestAndParseResponse
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineRequest
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineResponse
import ua.anastasiia.finesapp.output.pubsub.fine.FineCreatedEvent
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

@SpringBootTest
@ActiveProfiles("test")
class CreateFineNatsControllerTest {

    @Autowired
    lateinit var connection: Connection

    @Autowired
    lateinit var fineRepository: FineRepositoryOutPort

    @Test
    fun `should create fine and publish event when valid fine data is provided`() {
        // GIVEN
        val fineToCreate = getFineToSave().toProto()
        val createdEvent = connection.subscribe(NatsSubject.Fine.createdSubject(fineToCreate.car.plate))
        val expectedResponse = CreateFineResponse
            .newBuilder()
            .apply {
                successBuilder.setFine(fineToCreate)
            }.build()
        val expectedEvent = FineCreatedEvent.newBuilder().setFine(fineToCreate).build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Fine.CREATE,
            request = CreateFineRequest.newBuilder().setFine(fineToCreate).build(),
            parser = CreateFineResponse::parseFrom
        )
        val actualEvent = FineCreatedEvent.parseFrom(createdEvent.nextMessage(Duration.ofSeconds(10)).data)

        // THEN
        assertEquals(expectedResponse, actualResponse)
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `should return failure result to create fine when car plate already exists`() {
        // GIVEN
        val fine = Fine(
            car = Fine.Car(
                "test plate ${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}",
                "Test",
                "Test",
                "BLACK"
            ),
            trafficTickets = listOf()
        )
        val fineToCreate = fine.toProto()
        fineRepository.saveFine(fine).block()
        val expectedResponse =
            CreateFineResponse.newBuilder().apply { failureBuilder.carPlateDuplicateErrorBuilder }.build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Fine.CREATE,
            request = CreateFineRequest.newBuilder().setFine(fineToCreate).build(),
            parser = CreateFineResponse::parseFrom
        )

        // THEN
        assertTrue(actualResponse.hasFailure())
        assertEquals(expectedResponse, actualResponse)
    }
}
