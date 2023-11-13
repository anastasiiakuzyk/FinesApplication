package ua.anastasiia.finesapp.repository

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ua.anastasiia.finesapp.domain.Fine

@Repository
@Primary
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
}
