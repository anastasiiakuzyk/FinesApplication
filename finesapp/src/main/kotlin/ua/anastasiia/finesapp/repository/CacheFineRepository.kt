package ua.anastasiia.finesapp.repository

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ua.anastasiia.finesapp.domain.Fine

@Repository
@Primary
@Suppress("TooManyFunctions")
class CacheFineRepository(
    @Qualifier("mongoFineRepository") private val mongoFineRepository: MongoFineRepository,
    private val redisOperations: ReactiveRedisOperations<String, Fine>,
    @Value("\${spring.data.redis.key.prefix}") private val finePrefix: String
) : FineRepository by mongoFineRepository {

    override fun getFineById(fineId: ObjectId): Mono<Fine> {
        return redisOperations.opsForValue().get("$finePrefix$fineId").map { it }
            .switchIfEmpty {
                mongoFineRepository.getFineById(fineId)
                    .flatMap { savedMessage ->
                        redisOperations.opsForValue().set("$finePrefix$fineId", savedMessage)
                            .thenReturn(savedMessage)
                    }
            }
    }

    override fun getFineByCarPlate(plate: String): Mono<Fine> {
        return redisOperations.opsForValue().get("$finePrefix$plate").map { it }
            .switchIfEmpty {
                mongoFineRepository.getFineByCarPlate(plate)
                    .flatMap { savedMessage ->
                        redisOperations.opsForValue().set("$finePrefix$plate", savedMessage)
                            .thenReturn(savedMessage)
                    }
            }
    }

    override fun saveFine(mongoFine: Fine): Mono<Fine> {
        return mongoFineRepository.saveFine(mongoFine)
            .flatMap {
                redisOperations.opsForValue().set("$finePrefix${it.id!!}", it)
                    .thenReturn(it)
            }
    }

    override fun saveFines(mongoFines: List<Fine>): Flux<Fine> {
        return mongoFineRepository.saveFines(mongoFines)
            .flatMap {
                redisOperations.opsForValue().set("$finePrefix${it.id!!}", it)
                    .thenReturn(it)
            }
    }

    override fun deleteFineById(fineId: ObjectId): Mono<Fine> =
        redisOperations.opsForValue().delete("$finePrefix$fineId")
            .then(mongoFineRepository.deleteFineById(fineId))

    override fun addTrafficTicketByCarPlate(plate: String, newTicket: Fine.TrafficTicket): Mono<Fine> {
        return mongoFineRepository.addTrafficTicketByCarPlate(plate, newTicket)
            .flatMap {
                redisOperations.opsForValue().set("$finePrefix${it.id!!}", it)
                    .thenReturn(it)
            }
    }

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: ObjectId,
        updatedTicket: Fine.TrafficTicket
    ): Mono<Fine> {
        return mongoFineRepository.updateTrafficTicketByCarPlateAndId(plate, trafficTicketId, updatedTicket)
            .flatMap {
                redisOperations.opsForValue().set("$finePrefix${it.id!!}", it)
                    .thenReturn(it)
            }
    }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: ObjectId,
        violations: List<Fine.TrafficTicket.Violation>
    ): Mono<Fine> {
        return mongoFineRepository.addViolationToTrafficTicket(plate, trafficTicketId, violations)
            .flatMap {
                redisOperations.opsForValue().set("$finePrefix${it.id!!}", it)
                    .thenReturn(it)
            }
    }

    override fun removeViolationFromTicket(
        carPlate: String,
        ticketId: ObjectId,
        violationDescription: String
    ): Mono<Fine> {
        return mongoFineRepository.removeViolationFromTicket(carPlate, ticketId, violationDescription)
            .flatMap {
                redisOperations.opsForValue().set("$finePrefix${it.id!!}", it)
                    .thenReturn(it)
            }
    }

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: ObjectId): Mono<Fine> {
        return mongoFineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)
            .flatMap {
                redisOperations.opsForValue().set("$finePrefix${it.id!!}", it)
                    .thenReturn(it)
            }
    }

    override fun updateCarById(fineId: ObjectId, car: Fine.Car): Mono<Fine> {
        return mongoFineRepository.updateCarById(fineId, car)
            .flatMap {
                redisOperations.opsForValue().set("$finePrefix${it.id!!}", it)
                    .thenReturn(it)
            }
    }
}
