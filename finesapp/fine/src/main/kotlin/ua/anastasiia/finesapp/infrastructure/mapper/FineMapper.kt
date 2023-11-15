package ua.anastasiia.finesapp.infrastructure.mapper

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.adapters.repository.entity.MongoFine

fun Fine.toMongoFine() = MongoFine(
    id = id?.let { ObjectId(it) },
    car = car.toMongoCar(),
    trafficTickets = trafficTickets.map { it.toMongoTrafficTicket() }
)

fun Fine.Car.toMongoCar() = MongoFine.Car(
    plate = plate,
    make = make,
    model = model,
    color = color
)

fun Fine.TrafficTicket.toMongoTrafficTicket() = MongoFine.TrafficTicket(
    id = id?.let { ObjectId(it) } ?: ObjectId(),
    location = GeoJsonPoint(locationLon, locationLat),
    dateTime = dateTime,
    photoUrl = photoUrl,
    violations = violations.map { it.toMongoViolation() }
)

fun Fine.TrafficTicket.Violation.toMongoViolation() = MongoFine.TrafficTicket.Violation(
    description = description,
    price = price
)

fun MongoFine.toDomainFine() = Fine(
    id = id?.toHexString(),
    car = car.toDomainCar(),
    trafficTickets = trafficTickets.map { it.toDomainTrafficTicket() }
)

fun MongoFine.Car.toDomainCar() = Fine.Car(
    plate = plate,
    make = make,
    model = model,
    color = color
)

fun MongoFine.TrafficTicket.toDomainTrafficTicket() = Fine.TrafficTicket(
    id = id.toHexString(),
    locationLat = location.y,
    locationLon = location.x,
    dateTime = dateTime,
    photoUrl = photoUrl,
    violations = violations.map { it.toDomainViolation() }
)

fun MongoFine.TrafficTicket.Violation.toDomainViolation() = Fine.TrafficTicket.Violation(
    description = description,
    price = price
)
