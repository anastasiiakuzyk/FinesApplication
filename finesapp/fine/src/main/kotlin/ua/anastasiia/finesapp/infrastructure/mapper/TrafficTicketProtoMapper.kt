package ua.anastasiia.finesapp.infrastructure.mapper

import com.google.protobuf.util.Timestamps
import com.google.type.LatLng
import org.bson.types.ObjectId
import ua.anastasiia.finesapp.domain.Fine
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import ua.anastasiia.finesapp.commonmodels.trafficticket.TrafficTicket as ProtoTrafficTicket

fun Fine.TrafficTicket.toProto(): ProtoTrafficTicket {
    val builder = ProtoTrafficTicket.newBuilder()
        .setLocation(
            LatLng.newBuilder()
                .setLatitude(locationLat)
                .setLongitude(locationLon)
                .build()
        )
        .setDateTime(Timestamps.fromMillis(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()))
        .setPhotoUrl(photoUrl)
        .addAllViolations(violations.map { it.toProto() })
    id?.let {
        builder.setId(it)
    }
    return builder.build()
}

fun ProtoTrafficTicket.toTrafficTicket() = Fine.TrafficTicket(
    id = id ?: ObjectId().toHexString(),
    locationLat = location.latitude,
    locationLon = location.longitude,
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
