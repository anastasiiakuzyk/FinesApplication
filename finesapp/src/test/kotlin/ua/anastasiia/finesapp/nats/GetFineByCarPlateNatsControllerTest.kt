package ua.anastasiia.finesapp.nats

import io.nats.client.Connection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ua.anastasiia.finesapp.NatsSubject
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateResponse
import ua.anastasiia.finesapp.nats.NatsTestUtils.getCarPlate
import ua.anastasiia.finesapp.nats.NatsTestUtils.getFineToSave
import ua.anastasiia.finesapp.nats.NatsTestUtils.sendRequestAndParseResponse

@SpringBootTest
@ActiveProfiles("test")
class GetFineByCarPlateNatsControllerTest {

    @Autowired
    lateinit var connection: Connection

    @Autowired
    lateinit var fineRepository: FineRepositoryOutPort

    @Test
    fun `should return success fine by a specific car plate`() {
        // GIVEN
        val savedFine = fineRepository.saveFine(getFineToSave()).block()
        val expectedResponse = GetFineByCarPlateResponse
            .newBuilder()
            .apply { successBuilder.setFine(savedFine!!.toProto()) }
            .build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Fine.GET_BY_CAR_PLATE,
            request = GetFineByCarPlateRequest.newBuilder()
                .setCarPlate(savedFine!!.toProto().car.plate).build(),
            parser = GetFineByCarPlateResponse::parseFrom
        )

        // THEN
        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `should fail when fine is absent for a specific car plate`() {
        // GIVEN
        val carPlate = getCarPlate()
        val expectedResponse = GetFineByCarPlateResponse
            .newBuilder()
            .apply { failureBuilder.carPlateNotFoundErrorBuilder }
            .build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Fine.GET_BY_CAR_PLATE,
            request = GetFineByCarPlateRequest.newBuilder().setCarPlate(carPlate).build(),
            parser = GetFineByCarPlateResponse::parseFrom
        )

        // THEN
        assertTrue(actualResponse.hasFailure())
        assertEquals(expectedResponse, actualResponse)
    }
}
