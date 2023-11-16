package ua.anastasiia.finesapp.infrastructure.mapper

import ua.anastasiia.finesapp.domain.Fine

fun ViolationType.toViolation() = Fine.TrafficTicket.Violation(
    description = name.replace("_", " "),
    price = price
)

fun Int.toViolationType(): ViolationType {
    require(this in ViolationType.entries.indices) { "Invalid ID for ViolationType." }
    return ViolationType.entries[this]
}

fun Fine.TrafficTicket.Violation.toId(): Int = toViolationType().ordinal

fun Fine.TrafficTicket.Violation.toViolationType() =
    ViolationType.valueOf(description.replace(" ", "_"))
