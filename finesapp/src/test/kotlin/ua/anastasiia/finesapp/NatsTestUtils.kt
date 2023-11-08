package ua.anastasiia.finesapp

import com.google.protobuf.GeneratedMessageV3
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import ua.anastasiia.finesapp.dto.toViolation
import ua.anastasiia.finesapp.dto.toViolationType
import ua.anastasiia.finesapp.entity.MongoFine
import java.time.Duration
import java.time.LocalDateTime

object NatsTestUtils {
    fun getFineToSave() = MongoFine(
        id = ObjectId(),
        car = MongoFine.Car(getCarPlate(), "Test", "Test", "BLACK"),
        trafficTickets = listOf()
    )

    fun getCarPlate() = "test_${System.nanoTime()}"

    fun getTrafficTicketToSave() = MongoFine.TrafficTicket(
        location = GeoJsonPoint(0.0, 0.0),
        dateTime = LocalDateTime.now(),
        photoUrl = "test",
        violations = listOf(1, 2, 3).map { it.toViolationType().toViolation() }
    )

    fun <RequestT : GeneratedMessageV3, ResponseT : GeneratedMessageV3> sendRequestAndParseResponse(
        connection: Connection,
        subject: String,
        request: RequestT,
        parser: (ByteArray) -> ResponseT,
        timeout: Duration = Duration.ofSeconds(10L)
    ): ResponseT {
        return parser(connection.requestWithTimeout(subject, request.toByteArray(), timeout).get().data)
    }
}
