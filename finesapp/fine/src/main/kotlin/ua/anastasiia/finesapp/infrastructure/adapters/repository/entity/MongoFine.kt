package ua.anastasiia.finesapp.infrastructure.adapters.repository.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import ua.anastasiia.finesapp.infrastructure.adapters.repository.entity.MongoFine.Companion.COLLECTION_NAME
import java.time.LocalDateTime

@Document(collection = COLLECTION_NAME)
@TypeAlias("Fine")
data class MongoFine(
    @Id
    val id: ObjectId? = null,
    val car: Car,
    val trafficTickets: List<TrafficTicket>
) {

    data class Car(
        @Indexed(unique = true)
        val plate: String,
        val make: String,
        val model: String,
        val color: String
    )

    data class TrafficTicket(
        val id: ObjectId = ObjectId(),
        @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
        val location: GeoJsonPoint,
        val dateTime: LocalDateTime,
        val photoUrl: String,
        val violations: List<Violation>
    ) {

        data class Violation(
            val description: String,
            val price: Double
        )
    }

    companion object {
        const val COLLECTION_NAME = "fine"
    }
}
