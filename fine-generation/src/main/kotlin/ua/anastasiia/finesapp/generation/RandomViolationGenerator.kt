package ua.anastasiia.finesapp.generation

import ua.anastasiia.finesapp.generation.domain.Fine
import ua.anastasiia.finesapp.generation.domain.ViolationType
import ua.anastasiia.finesapp.generation.domain.toViolation
import kotlin.random.Random

class RandomViolationGenerator : RandomFieldGenerator<List<Fine.TrafficTicket.Violation>> {

    override fun generate(number: Int): List<List<Fine.TrafficTicket.Violation>> {
        return List(number) {
            val violations = mutableSetOf<Fine.TrafficTicket.Violation>()
            repeat(Random.nextInt(1, 4)) {
                violations.add(ViolationType.entries[Random.nextInt(ViolationType.entries.size)].toViolation())
            }
            violations.toList()
        }
    }
}
