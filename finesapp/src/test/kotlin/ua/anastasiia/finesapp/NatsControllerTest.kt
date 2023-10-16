package ua.anastasiia.finesapp

import com.google.protobuf.GeneratedMessageV3
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.test.context.ActiveProfiles
import ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration.RandomPlateGenerator
import ua.anastasiia.finesapp.controller.nats.CreateFineNatsController
import ua.anastasiia.finesapp.dto.toViolation
import ua.anastasiia.finesapp.dto.toViolationType
import ua.anastasiia.finesapp.entity.MongoFine
import ua.anastasiia.finesapp.repository.MongoFineRepository
import java.time.Duration
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("local")
abstract class NatsControllerTest {

    @Autowired
    protected lateinit var createFineNatsController: CreateFineNatsController

    @Autowired
    protected lateinit var connection: Connection

    @Autowired
    protected lateinit var fineRepository: MongoFineRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun clean() {
        mongoTemplate.dropCollection<MongoFine>()
    }

    protected fun getFineToSaveGeneratedCarPlate() = MongoFine(
        id = ObjectId(),
        car = MongoFine.Car(RandomPlateGenerator().generate(), "Test", "Test", "BLACK"),
        trafficTickets = listOf()
    )

    protected fun getFineToSave() = MongoFine(
        id = ObjectId(),
        car = MongoFine.Car("TE1234ST", "Test", "Test", "BLACK"),
        trafficTickets = listOf()
    )

    protected fun getTrafficTicketToSave() = MongoFine.TrafficTicket(
        location = GeoJsonPoint(0.0, 0.0),
        dateTime = LocalDateTime.now(),
        photoUrl = "test",
        violations = listOf(1, 2, 3).map { it.toViolationType().toViolation() }
    )

    protected fun <RequestT : GeneratedMessageV3, ResponseT : GeneratedMessageV3> sendRequestAndParseResponse(
        subject: String,
        request: RequestT,
        parser: (ByteArray) -> ResponseT,
        timeout: Duration = Duration.ofSeconds(10L)
    ): ResponseT {
        return parser(connection.requestWithTimeout(subject, request.toByteArray(), timeout).get().data)
    }
}
