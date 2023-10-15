package ua.anastasiia.finesapp.dto

import com.google.protobuf.util.Timestamps
import com.google.type.LatLng
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import ua.anastasiia.finesapp.commonmodels.car.Car
import ua.anastasiia.finesapp.commonmodels.fine.Fine
import ua.anastasiia.finesapp.commonmodels.trafficticket.TrafficTicket
import ua.anastasiia.finesapp.commonmodels.violation.Violation
import ua.anastasiia.finesapp.entity.MongoFine
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun MongoFine.toProto(): Fine =
    Fine.newBuilder()
        .setId(id?.toHexString() ?: ObjectId().toHexString())
        .setCar(car.toProto())
        .addAllTrafficTickets(trafficTickets.map { it.toProto() })
        .build()

fun Fine.toFine() = MongoFine(
    id = id?.let { ObjectId(id) },
    car = car.toCar(),
    trafficTickets = trafficTicketsList.map { it.toTrafficTicket() }
)

fun MongoFine.Car.toProto(): Car =
    Car.newBuilder()
        .setPlate(plate)
        .setMake(make)
        .setModel(model)
        .setColor(color)
        .build()

fun Car.toCar() = MongoFine.Car(
    plate = plate,
    make = make,
    model = model,
    color = color
)

fun MongoFine.TrafficTicket.toProto(): TrafficTicket = TrafficTicket.newBuilder()
    .setId(id.toHexString())
    .setLocation(
        LatLng.newBuilder()
            .setLatitude(location.x)
            .setLongitude(location.y)
            .build()
    )
    .setDateTime(
        Timestamps.fromMillis(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli())
    )
    .setPhotoUrl(photoUrl)
    .addAllViolations(violations.map { violation -> violation.toProto() })
    .build()

fun TrafficTicket.toTrafficTicket() = MongoFine.TrafficTicket(
    id = id?.let { ObjectId(id) } ?: ObjectId(),
    location = GeoJsonPoint(location.latitude, location.longitude),
    dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(
            dateTime.seconds,
            dateTime.nanos.toLong()
        ),
        ZoneOffset.UTC
    ),
    photoUrl = photoUrl,
    violations = violationsList.map { it.toViolation() }
)

fun MongoFine.TrafficTicket.Violation.toProto(): Violation =
    Violation.newBuilder()
        .setDescription(description)
        .setPrice(price)
        .build()

fun Violation.toViolation() = MongoFine.TrafficTicket.Violation(
    description = description,
    price = price
)
