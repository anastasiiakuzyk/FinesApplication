package ua.anastasiia.finesapp.domain.converter

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.generation.domain.Fine as GeneratedFine

fun GeneratedFine.toFine() =
    Fine(
        id = id,
        car = Fine.Car(
            plate = car.plate,
            make = car.make,
            model = car.model,
            color = car.color
        ),
        trafficTickets = trafficTickets.map { ticket ->
            Fine.TrafficTicket(
                id = ticket.id,
                locationLat = ticket.locationLat,
                locationLon = ticket.locationLon,
                dateTime = ticket.dateTime,
                photoUrl = ticket.photoUrl,
                violations = ticket.violations.map { violation ->
                    Fine.TrafficTicket.Violation(
                        description = violation.description,
                        price = violation.price
                    )
                },
                valid = true
            )
        }
    )

