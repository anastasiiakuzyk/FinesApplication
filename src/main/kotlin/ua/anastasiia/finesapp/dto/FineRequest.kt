package ua.anastasiia.finesapp.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import ua.anastasiia.finesapp.annotation.EnumValidator
import ua.anastasiia.finesapp.annotation.IntListValidator
import ua.anastasiia.finesapp.entity.MongoFine
import java.time.LocalDateTime

data class FineRequest(
    @field:Valid
    val car: CarRequest,
    @field:Valid
    val trafficTickets: List<TrafficTicketRequest>
)

data class CarRequest(
    @field:Pattern(regexp = "^(?=(.*[A-ZА-ЯІЇҐЄ]){2,})([A-ZА-ЯІЇҐЄ0-9]{3,8})\$")
    val plate: String,
    @field:Size(min = 1, max = 50)
    val make: String,
    @field:Size(min = 1, max = 50)
    val model: String?,
    @field:EnumValidator(CarColor::class)
    val color: String
) {
    enum class CarColor {
        WHITE,
        BLACK,
        SILVER,
        RED,
        BLUE,
        GREEN,
        BROWN,
        YELLOW
    }
}

fun CarRequest.toMongoCar() = MongoFine.Car(
    plate = plate,
    make = make,
    model = model!!,
    color = color
)

data class TrafficTicketRequest(
    val id: ObjectId? = null,
    val longitude: Double,
    val latitude: Double,
    val dateTime: LocalDateTime,
    val photoUrl: String,
    @field:IntListValidator(0, 9)
    val violations: List<Int>
)

@Suppress("MagicNumber")
enum class Violation(val price: Double) {
    VIOLATION_OF_LICENSE_PLATE_USE(1190.0),
    VIOLATION_OF_SIGNS(340.0),
    PARKED_IN_TWO_LANES(680.0),
    PARKED_IN_FORBIDDEN_AREAS(680.0),
    OBSTRUCTS_TRAFFIC_PEDESTRIANS(680.0),
    PARKED_ON_PUBLIC_TRANSPORT_LANE(680.0),
    PARKED_ON_BIKE_LANE(680.0),
    OBSTRUCTS_MUNICIPAL_TRANSPORT_MOVEMENT(680.0),
    VIOLATES_PARKING_SCHEME(680.0),
    PARKED_IN_DISABLED_ZONE(1700.0)
}

fun FineRequest.toMongoFine() = MongoFine(
    car = MongoFine.Car(
        plate = car.plate,
        make = car.make,
        model = car.model!!,
        color = car.color.uppercase()
    ),
    trafficTickets = trafficTickets.map { trafficTicketRequest ->
        trafficTicketRequest.toMongoTrafficTicket()
    }
)

fun TrafficTicketRequest.toMongoTrafficTicket() = MongoFine.TrafficTicket(
    id = id ?: ObjectId(),
    location = GeoJsonPoint(longitude, latitude),
    dateTime = dateTime,
    photoUrl = photoUrl,
    violations = getViolationsByIndexes(violations)
)

fun getViolationsByIndexes(violations: List<Int>) = violations.map { violationIndex ->
    Violation.entries[violationIndex].toMongoViolation()
}

fun Violation.toMongoViolation() = MongoFine.TrafficTicket.Violation(
    description = this.name.replace("_", " "),
    price = this.price
)
