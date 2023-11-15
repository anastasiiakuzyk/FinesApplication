package ua.anastasiia.finesapp

import io.nats.client.Connection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.test.context.ActiveProfiles
import ua.anastasiia.finesapp.NatsTestUtils.getFineToSave
import ua.anastasiia.finesapp.NatsTestUtils.sendRequestAndParseResponse
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOut
import ua.anastasiia.finesapp.infrastructure.adapters.repository.entity.MongoFine
import ua.anastasiia.finesapp.infrastructure.mapper.toCar
import ua.anastasiia.finesapp.infrastructure.mapper.toProto
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsRequest
import ua.anastasiia.finesapp.input.reqreply.car.GetAllCarsResponse

@SpringBootTest
@ActiveProfiles("test")
class GetAllCarsNatsControllerTest {

    @Autowired
    lateinit var connection: Connection

    @Autowired
    lateinit var fineRepository: FineRepositoryOut

    @Autowired
    lateinit var reactiveMongoTemplate: ReactiveMongoTemplate

    @Test
    fun `should return success result`() {
        // GIVEN
        fineRepository.saveFines(listOf(getFineToSave())).collectList().block()
        val expectedResponse = GetAllCarsResponse
            .newBuilder()
            .apply {
                successBuilder.addAllCars(
                    fineRepository.getAllCars()
                        .collectList()
                        .block()!!
                        .map { it.toCar().toProto() }
                )
            }
            .build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Car.GET_ALL,
            request = GetAllCarsRequest.newBuilder().build(),
            parser = GetAllCarsResponse::parseFrom
        )

        // THEN
        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `should return failure result when no available cars`() {
        // GIVEN
        reactiveMongoTemplate.dropCollection<MongoFine>().block()
        val expectedResponse = GetAllCarsResponse
            .newBuilder()
            .apply { failureBuilder.carsNotFoundErrorBuilder }
            .build()

        // WHEN
        val actualResponse = sendRequestAndParseResponse(
            connection = connection,
            subject = NatsSubject.Car.GET_ALL,
            request = GetAllCarsRequest.newBuilder().build(),
            parser = GetAllCarsResponse::parseFrom
        )

        // THEN
        assertEquals(expectedResponse, actualResponse)
    }
}
