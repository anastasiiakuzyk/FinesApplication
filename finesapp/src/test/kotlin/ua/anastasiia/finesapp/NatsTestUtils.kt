package ua.anastasiia.finesapp

import com.google.protobuf.GeneratedMessageV3
import io.nats.client.Connection
import org.bson.types.ObjectId
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toViolation
import ua.anastasiia.finesapp.infrastructure.mapper.toViolationType
import java.time.Duration
import java.time.LocalDateTime

object NatsTestUtils {
    fun getFineToSave() = Fine(
        id = ObjectId().toHexString(),
        car = Fine.Car(getCarPlate(), "Test", "Test", "BLACK"),
        trafficTickets = listOf()
    )

    fun getCarPlate() = "test_${System.nanoTime()}"

    fun getTrafficTicketToSave() = Fine.TrafficTicket(
        id = ObjectId().toHexString(),
        locationLat = 0.0,
        locationLon = 0.0,
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
