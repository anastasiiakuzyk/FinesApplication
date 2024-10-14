package ua.anastasiia.finesapp.generation

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ThreadLocalRandom

class RandomDateTimeGenerator : RandomFieldGenerator<LocalDateTime> {

    override fun generate(number: Int): List<LocalDateTime> {
        val now = LocalDateTime.now()
        val minEpochSecond = now.minusYears(1).toEpochSecond(ZoneOffset.UTC)
        val maxEpochSecond = now.toEpochSecond(ZoneOffset.UTC)

        return List(number) {
            val randomEpochSecond = ThreadLocalRandom.current().nextLong(minEpochSecond, maxEpochSecond)
            LocalDateTime.ofEpochSecond(randomEpochSecond, 0, ZoneOffset.UTC)
        }
    }
}
