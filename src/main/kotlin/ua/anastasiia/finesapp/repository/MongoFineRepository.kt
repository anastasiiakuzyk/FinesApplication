package ua.anastasiia.finesapp.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import ua.anastasiia.finesapp.dto.TotalFineSumResponse
import ua.anastasiia.finesapp.entity.MongoFine
import ua.anastasiia.finesapp.entity.MongoFine.Companion.COLLECTION_NAME
import ua.anastasiia.finesapp.util.findAndModify
import ua.anastasiia.finesapp.util.findAndRemove
import java.time.LocalDate

@Repository
@Suppress("TooManyFunctions")
class MongoFineRepository(val mongoTemplate: MongoTemplate) {
    fun getAllFines(): List<MongoFine> = mongoTemplate.findAll<MongoFine>()
    fun getAllFinesInLocation(longitude: Double, latitude: Double): List<MongoFine> {
        return mongoTemplate.find(
            Query(
                Criteria.where("trafficTickets.longitude").`is`(longitude).and("trafficTickets.latitude").`is`(latitude)
            )
        )
    }

    fun getAllFinesByDate(localDate: LocalDate): List<MongoFine> {
        val start = localDate.atStartOfDay()
        val end = localDate.plusDays(1).atStartOfDay()
        return mongoTemplate.find<MongoFine>(Query(Criteria.where("trafficTickets.dateTime").gte(start).lt(end)))
    }

    fun getFineById(fineId: ObjectId): MongoFine? =
        mongoTemplate.findOne<MongoFine>(Query(Criteria.where("id").`is`(fineId)))

    fun getFineByCarPlate(plate: String): MongoFine? =
        mongoTemplate.findOne<MongoFine>(Query.query(Criteria.where("car.plate").`is`(plate)))

    fun saveFine(mongoFine: MongoFine): MongoFine =
        mongoTemplate.save<MongoFine>(mongoFine)

    fun saveFines(mongoFines: List<MongoFine>): List<MongoFine> =
        mongoTemplate.insertAll(mongoFines).toList()

    fun deleteFineById(fineId: ObjectId): MongoFine? =
        mongoTemplate.findAndRemove<MongoFine>(Query(Criteria.where("id").`is`(fineId)))

    fun addTrafficTicketByCarPlate(plate: String, newTicket: MongoFine.TrafficTicket): MongoFine? =
        mongoTemplate.findAndModify<MongoFine>(
            Query.query(Criteria.where("car.plate").`is`(plate)),
            Update().push("trafficTickets", newTicket)
        )

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicket: MongoFine.TrafficTicket
    ): MongoFine? = mongoTemplate.findAndModify<MongoFine>(
        Query.query(Criteria.where("car.plate").`is`(plate).and("trafficTickets.id").`is`(trafficTicketId)),
        Update().set("trafficTickets.$", updatedTicket)
    )

    fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: ObjectId,
        violations: List<MongoFine.TrafficTicket.Violation>
    ): MongoFine? = mongoTemplate.findAndModify<MongoFine>(
        Query.query(Criteria.where("car.plate").`is`(plate).and("trafficTickets.id").`is`(trafficTicketId)),
        Update().apply {
            addToSet("trafficTickets.$.violations").each(violations)
        }
    )

    fun removeViolationFromTicket(ticketId: ObjectId, violationDescription: String): MongoFine? =
        mongoTemplate.findAndModify<MongoFine>(
            Query(Criteria.where("trafficTickets.id").`is`(ticketId)),
            Update().pull("trafficTickets.$.violations", mapOf("description" to violationDescription))
        )

    fun getSumOfFinesForCarPlate(plate: String): TotalFineSumResponse? {
        val matchStage = match(Criteria.where("car.plate").`is`(plate))
        val unwindTickets = unwind("trafficTickets")
        val unwindViolations = unwind("trafficTickets.violations")
        val groupStage = group("car.plate")
            .sum("trafficTickets.violations.price").`as`("totalSum")

        return mongoTemplate.aggregate<MongoFine, TotalFineSumResponse>(
            Aggregation.newAggregation(
                matchStage,
                unwindTickets,
                unwindViolations,
                groupStage
            )
        ).mappedResults.run {
            this.firstOrNull()
        }
    }

    fun getAllCars(): List<MongoFine.Car> {
        val query = Query().apply {
            fields().include("car").exclude("_id")
        }

        return mongoTemplate.find(query, MongoFine.Car::class.java, COLLECTION_NAME)
//            .mapNotNull { linkedHashMap ->
//                (linkedHashMap["car"] as? LinkedHashMap<*, *>)?.let {
//                    MongoFine.Car(
//                        plate = it["plate"] as String,
//                        make = it["make"] as String,
//                        model = it["model"] as String,
//                        color = it["color"] as String
//                    )
//                }
//            }
    }

    fun updateCarById(fineId: ObjectId, car: MongoFine.Car): MongoFine? =
        mongoTemplate.findAndModify<MongoFine>(Query(Criteria.where("id").`is`(fineId)), Update().set("car", car))
}
