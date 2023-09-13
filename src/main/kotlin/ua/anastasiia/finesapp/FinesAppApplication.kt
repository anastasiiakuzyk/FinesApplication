package ua.anastasiia.finesapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FinesAppApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<FinesAppApplication>(*args)
}
