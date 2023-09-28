package ua.anastasiia.finesapp.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import ua.anastasiia.finesapp.entity.MongoFine.Companion.COLLECTION_NAME
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
        @Field("car.plate")
        val plate: String,
        @Field("car.make")
        val make: String,
        @Field("car.model")
        val model: String,
        @Field("car.color")
        val color: String
    )

    data class TrafficTicket(
        val id: ObjectId = ObjectId(),
        val longitude: Double,
        val latitude: Double,
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
