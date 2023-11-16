package ua.anastasiia.finesapp.infrastructure.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.commonmodels.violation.Violation as ProtoViolation

fun Fine.TrafficTicket.Violation.toProto(): ProtoViolation =
    ProtoViolation.newBuilder()
        .setDescription(description)
        .setPrice(price)
        .build()

fun ProtoViolation.toViolation() = Fine.TrafficTicket.Violation(
    description = description,
    price = price
)
