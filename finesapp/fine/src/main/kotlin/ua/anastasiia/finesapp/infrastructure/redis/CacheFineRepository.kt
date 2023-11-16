package ua.anastasiia.finesapp.infrastructure.redis

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.domain.Fine

@Repository
@Primary
@Suppress("TooManyFunctions")
class CacheFineRepository(
    @Qualifier("mongoFineRepository") private val fineRepository: FineRepositoryOutPort,
    private val redisOperations: ReactiveRedisOperations<String, Fine>,
    @Value("\${spring.data.redis.key.prefix}") private val finePrefix: String
) : FineRepositoryOutPort by fineRepository {

    override fun getFineById(fineId: String): Mono<Fine> {
        return redisOperations.opsForValue().get("$finePrefix$fineId")
            .switchIfEmpty {
                fineRepository.getFineById(fineId)
                    .flatMap { saveFineByIdToRedis(it) }
            }
    }

    override fun getFineByCarPlate(plate: String): Mono<Fine> {
        return redisOperations.opsForValue().get("$finePrefix$plate")
            .switchIfEmpty {
                fineRepository.getFineByCarPlate(plate)
                    .flatMap {
                        redisOperations.opsForValue().set("$finePrefix$plate", it)
                            .thenReturn(it)
                    }
            }
    }

    override fun saveFine(fine: Fine): Mono<Fine> {
        return fineRepository.saveFine(fine)
            .flatMap { saveFineByIdToRedis(it) }
    }

    override fun saveFines(fines: List<Fine>): Flux<Fine> {
        return fineRepository.saveFines(fines)
            .flatMap { saveFineByIdToRedis(it) }
    }

    override fun deleteFineById(fineId: String): Mono<Fine> =
        redisOperations.opsForValue().delete("$finePrefix$fineId")
            .then(fineRepository.deleteFineById(fineId))

    override fun addTrafficTicketByCarPlate(plate: String, newTicket: Fine.TrafficTicket): Mono<Fine> {
        return fineRepository.addTrafficTicketByCarPlate(plate, newTicket)
            .flatMap { saveFineByIdToRedis(it) }
    }

    override fun updateTrafficTicketByCarPlateAndId(
        plate: String,
        trafficTicketId: String,
        updatedTicket: Fine.TrafficTicket
    ): Mono<Fine> {
        return fineRepository.updateTrafficTicketByCarPlateAndId(plate, trafficTicketId, updatedTicket)
            .flatMap { saveFineByIdToRedis(it) }
    }

    override fun addViolationToTrafficTicket(
        plate: String,
        trafficTicketId: String,
        violations: List<Fine.TrafficTicket.Violation>
    ): Mono<Fine> {
        return fineRepository.addViolationToTrafficTicket(plate, trafficTicketId, violations)
            .flatMap { saveFineByIdToRedis(it) }
    }

    override fun removeViolationFromTicket(
        carPlate: String,
        ticketId: String,
        violationDescription: String
    ): Mono<Fine> {
        return fineRepository.removeViolationFromTicket(carPlate, ticketId, violationDescription)
            .flatMap { saveFineByIdToRedis(it) }
    }

    override fun removeTicketByCarPlateAndId(carPlate: String, ticketId: String): Mono<Fine> {
        return fineRepository.removeTicketByCarPlateAndId(carPlate, ticketId)
            .flatMap { saveFineByIdToRedis(it) }
    }

    override fun updateCarById(fineId: String, car: Fine.Car): Mono<Fine> {
        return fineRepository.updateCarById(fineId, car)
            .flatMap { saveFineByIdToRedis(it) }
    }

    private fun saveFineByIdToRedis(fineToSave: Fine): Mono<Fine> {
        return redisOperations.opsForValue().set("$finePrefix${fineToSave.id}", fineToSave)
            .thenReturn(fineToSave)
    }
}
