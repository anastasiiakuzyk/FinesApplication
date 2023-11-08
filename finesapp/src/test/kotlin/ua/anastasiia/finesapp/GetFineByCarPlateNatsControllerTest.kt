package ua.anastasiia.finesapp

import io.nats.client.Connection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ua.anastasiia.finesapp.NatsTestUtils.getCarPlate
import ua.anastasiia.finesapp.NatsTestUtils.getFineToSave
import ua.anastasiia.finesapp.NatsTestUtils.sendRequestAndParseResponse
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateRequest
import ua.anastasiia.finesapp.input.reqreply.fine.GetFineByCarPlateResponse
import ua.anastasiia.finesapp.repository.MongoFineRepository

@SpringBootTest
@ActiveProfiles("test")
class GetFineByCarPlateNatsControllerTest {

    @Autowired
    lateinit var connection: Connection

    @Autowired
    lateinit var fineRepository: MongoFineRepository

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
            request = GetFineByCarPlateRequest.newBuilder().setCarPlate(savedFine!!.toProto().car.plate).build(),
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
