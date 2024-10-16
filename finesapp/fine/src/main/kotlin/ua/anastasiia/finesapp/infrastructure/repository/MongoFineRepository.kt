package ua.anastasiia.finesapp.infrastructure.repository

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
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.mapper.toDomainFine
import ua.anastasiia.finesapp.infrastructure.mapper.toMongoFine
import ua.anastasiia.finesapp.infrastructure.mapper.toMongoTrafficTicket
import ua.anastasiia.finesapp.infrastructure.mapper.toMongoViolation
import ua.anastasiia.finesapp.infrastructure.repository.entity.MongoFine
import ua.anastasiia.finesapp.infrastructure.repository.entity.MongoFine.Companion.COLLECTION_NAME
import ua.anastasiia.finesapp.infrastructure.repository.util.findAndModify
import ua.anastasiia.finesapp.infrastructure.repository.util.findAndRemove
import java.time.LocalDate

@Repository
@Suppress("TooManyFunctions")
class MongoFineRepository(private val reactiveMongoTemplate: ReactiveMongoTemplate) : FineRepositoryOutPort {
    override fun getAllFines(): Flux<Fine> = reactiveMongoTemplate.findAll<MongoFine>().map { it.toDomainFine() }
    override fun getAllFinesInLocation(longitude: Double, latitude: Double, radiusInMeters: Double): Flux<Fine> =
        reactiveMongoTemplate.find<MongoFine>(
            Query(
                Criteria.where("trafficTickets.location").nearSphere(
                    GeoJsonPoint(longitude, latitude)
                ).maxDistance(radiusInMeters)
            )
        ).map { it.toDomainFine() }

    override fun getAllFinesByDate(localDate: LocalDate): Flux<Fine> {
        val start = localDate.atStartOfDay()
        val end = localDate.plusDays(1).atStartOfDay()
        return reactiveMongoTemplate.find<MongoFine>(
            Query(Criteria.where("trafficTickets.dateTime").gte(start).lt(end))
        ).map {
            it.toDomainFine()
        }
    }

    override fun getFineById(fineId: String): Mono<Fine> =
        reactiveMongoTemplate.findOne<MongoFine>(Query(Criteria.where("id").`is`(fineId)))
            .map { it.toDomainFine() }

    override fun getFineByCarPlate(plate: String): Mono<Fine> =
        reactiveMongoTemplate.findOne<MongoFine>(Query.query(Criteria.where("car.plate").`is`(plate)))
            .map { it.toDomainFine() }

    override fun saveFine(fine: Fine): Mono<Fine> =
        reactiveMongoTemplate.save<MongoFine>(fine.toMongoFine()).map { it.toDomainFine() }

    override fun saveFines(fines: List<Fine>): Flux<Fine> =
        reactiveMongoTemplate.insertAll<MongoFine>(fines.map { it.toMongoFine() }).map { it.toDomainFine() }

    override fun deleteFineById(fineId: String): Mono<Fine> =
        reactiveMongoTemplate.findAndRemove<MongoFine>(Query(Criteria.where("id").`is`(fineId)))
            .map { it.toDomainFine() }

    override fun getFineByCarPlateAndTicketId(
        carPlate: String,
        ticketId: String,
    ): Mono<Fine> {
        return reactiveMongoTemplate.findOne<MongoFine>(
            Query.query(
                Criteria.where("car.plate").`is`(carPlate)
                    .and("trafficTickets").elemMatch(Criteria.where("id").`is`(ObjectId(ticketId)))
            )
        ).map { it.toDomainFine() }
    }

    override fun addTrafficTicketByCarPlate(plate: String, newTicket: Fine.TrafficTicket): Mono<Fine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query.query(Criteria.where("car.plate").`is`(plate)),
            Update().push("trafficTickets", newTicket.toMongoTrafficTicket())
        ).map { it.toDomainFine() }

    override fun deleteTrafficTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query.query(Criteria.where("car.plate").`is`(carPlate).and("trafficTickets.id").`is`(ticketId)),
            Update().pull("trafficTickets", BasicDBObject("id", ticketId))
        ).map { it.toDomainFine() }

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: String,
        updatedTicket: Fine.TrafficTicket,
    ): Mono<Fine> = reactiveMongoTemplate.findAndModify<MongoFine>(
        Query.query(Criteria.where("car.plate").`is`(plate).and("trafficTickets.id").`is`(trafficTicketId)),
        Update().set("trafficTickets.$", updatedTicket.toMongoTrafficTicket())
    ).map { it.toDomainFine() }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: String,
        violations: List<Fine.TrafficTicket.Violation>,
    ): Mono<Fine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query.query(
                Criteria
                    .where("car.plate")
                    .`is`(plate)
                    .and("trafficTickets.id")
                    .`is`(trafficTicketId)
            ),
            Update().apply {
                addToSet("trafficTickets.$.violations").each(violations.map { it.toMongoViolation() })
            }
        ).map { it.toDomainFine() }

    override fun removeViolationFromTicket(
        carPlate: String,
        ticketId: String,
        violationDescription: String,
    ): Mono<Fine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query(Criteria.where("trafficTickets.id").`is`(ticketId).and("car.plate").`is`(carPlate)),
            Update().pull("trafficTickets.$.violations", mapOf("description" to violationDescription))
        ).map { it.toDomainFine() }

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query(Criteria.where("trafficTickets.id").`is`(ticketId).and("car.plate").`is`(carPlate)),
            Update().pull("trafficTickets", BasicDBObject("id", ticketId))
        ).map { it.toDomainFine() }

    override fun getSumOfFinesForCarPlate(carPlate: String): Mono<Double> {
        val matchStage = match(Criteria.where("car.plate").`is`(carPlate))
        val unwindTickets = unwind("trafficTickets")
        val unwindViolations = unwind("trafficTickets.violations")
        val groupStage = group("car.plate").sum("trafficTickets.violations.price").`as`("totalSum")

        return reactiveMongoTemplate.aggregate<MongoFine, Double>(
            Aggregation.newAggregation(
                matchStage,
                unwindTickets,
                unwindViolations,
                groupStage
            )
        ).next()
    }

    override fun getAllCars(): Flux<Fine.Car> = reactiveMongoTemplate.aggregate<Fine.Car>(
        Aggregation.newAggregation(
            project("car.plate", "car.make", "car.model", "car.color")
                .andExclude("_id")
        ),
        COLLECTION_NAME
    )

    override fun updateCarById(fineId: String, car: Fine.Car): Mono<Fine> =
        reactiveMongoTemplate.findAndModify<MongoFine>(
            Query(Criteria.where("id").`is`(fineId)),
            Update().set("car", car)
        ).map { it.toDomainFine() }
}
