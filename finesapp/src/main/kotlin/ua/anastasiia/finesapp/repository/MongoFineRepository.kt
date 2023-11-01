package ua.anastasiia.finesapp.repository

import com.mongodb.BasicDBObject
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ua.anastasiia.finesapp.dto.response.CarResponse
import ua.anastasiia.finesapp.dto.response.TotalFineSumResponse
import ua.anastasiia.finesapp.entity.MongoFine
import ua.anastasiia.finesapp.entity.MongoFine.Companion.COLLECTION_NAME
import ua.anastasiia.finesapp.util.findAndModify
import ua.anastasiia.finesapp.util.findAndRemove
import java.time.LocalDate

@Repository
@Suppress("TooManyFunctions")
class MongoFineRepository(val reactiveMongoTemplate: ReactiveMongoTemplate) {
    fun getAllFines(): Flux<MongoFine> = reactiveMongoTemplate.findAll<MongoFine>()
    fun getAllFinesInLocation(longitude: Double, latitude: Double, radiusInMeters: Double): Flux<MongoFine> =
        reactiveMongoTemplate.find(
            Query(
                Criteria.where("trafficTickets.location").nearSphere(
                    GeoJsonPoint(longitude, latitude)
                ).maxDistance(radiusInMeters)
            )
        )

    fun getAllFinesByDate(localDate: LocalDate): Flux<MongoFine> {
        val start = localDate.atStartOfDay()
        val end = localDate.plusDays(1).atStartOfDay()
        return reactiveMongoTemplate.find<MongoFine>(
            Query(Criteria.where("trafficTickets.dateTime").gte(start).lt(end))
        )
    }

    fun getFineById(fineId: ObjectId): Mono<MongoFine> =
        reactiveMongoTemplate.findOne<MongoFine>(Query(Criteria.where("id").`is`(fineId)))

    fun getFineByCarPlate(plate: String): Mono<MongoFine> =
        reactiveMongoTemplate.findOne<MongoFine>(Query.query(Criteria.where("car.plate").`is`(plate)))

    fun saveFine(mongoFine: MongoFine): Mono<MongoFine> = reactiveMongoTemplate.save(mongoFine)

    fun saveFines(mongoFines: List<MongoFine>): Flux<MongoFine> = reactiveMongoTemplate.insertAll(mongoFines)

    fun deleteFineById(fineId: ObjectId): Mono<MongoFine> =
        reactiveMongoTemplate.findAndRemove<MongoFine>(Query(Criteria.where("id").`is`(fineId)))

    fun addTrafficTicketByCarPlate(plate: String, newTicket: MongoFine.TrafficTicket): Mono<MongoFine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query.query(Criteria.where("car.plate").`is`(plate)),
            Update().push("trafficTickets", newTicket)
        )

    fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicket: MongoFine.TrafficTicket
    ): Mono<MongoFine> = reactiveMongoTemplate.findAndModify<MongoFine>(
        Query.query(Criteria.where("car.plate").`is`(plate).and("trafficTickets.id").`is`(trafficTicketId)),
        Update().set("trafficTickets.$", updatedTicket)
    )

    fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: ObjectId,
        violations: List<MongoFine.TrafficTicket.Violation>
    ): Mono<MongoFine> = reactiveMongoTemplate.findAndModify<MongoFine>(
        Query.query(
            Criteria
                .where("car.plate")
                .`is`(plate)
                .and("trafficTickets.id")
                .`is`(trafficTicketId)
        ),
        Update().apply {
            addToSet("trafficTickets.$.violations").each(violations)
        }
    )

    fun removeViolationFromTicket(carPlate: String, ticketId: ObjectId, violationDescription: String): Mono<MongoFine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query(Criteria.where("trafficTickets.id").`is`(ticketId).and("car.plate").`is`(carPlate)),
            Update().pull("trafficTickets.$.violations", mapOf("description" to violationDescription))
        )

    fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): Mono<MongoFine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query(Criteria.where("trafficTickets.id").`is`(ticketId).and("car.plate").`is`(carPlate)),
            Update().pull("trafficTickets", BasicDBObject("id", ticketId))
        )

    fun getSumOfFinesForCarPlate(carPlate: String): Mono<TotalFineSumResponse> {
        val matchStage = match(Criteria.where("car.plate").`is`(carPlate))
        val unwindTickets = unwind("trafficTickets")
        val unwindViolations = unwind("trafficTickets.violations")
        val groupStage = group("car.plate").sum("trafficTickets.violations.price").`as`("totalSum")

        return reactiveMongoTemplate.aggregate<MongoFine, TotalFineSumResponse>(
            Aggregation.newAggregation(
                matchStage,
                unwindTickets,
                unwindViolations,
                groupStage
            )
        ).next()
    }

    fun getAllCars(): Flux<CarResponse> = reactiveMongoTemplate.aggregate<CarResponse>(
        Aggregation.newAggregation(
            project("car.plate", "car.make", "car.model", "car.color")
                .andExclude("_id")
        ),
        COLLECTION_NAME
    )

    fun updateCarById(fineId: ObjectId, car: MongoFine.Car): Mono<MongoFine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query(Criteria.where("id").`is`(fineId)),
            Update().set("car", car)
        )
}
