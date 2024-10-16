package ua.anastasiia.finesapp.generation.domain

@Suppress("MagicNumber")
enum class ViolationType(val price: Double) {
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

fun ViolationType.toViolation() = Fine.TrafficTicket.Violation(
    description = name.replace("_", " "),
    price = price
)
