package ua.anastasiia.finesapp.application.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.application.port.output.TrafficTicketAddedEventProducerOutPort
import ua.anastasiia.finesapp.domain.Fine

class FineServiceGetFineByCarPlateTest {

    private val fineRepository: FineRepositoryOutPort = mockk()
    private val fineKafkaProducer: TrafficTicketAddedEventProducerOutPort = mockk()
    private val fineService: FineService = FineService(fineRepository, fineKafkaProducer)

    @Test
    fun `should return fine when fine found by car plate`() {
        // GIVEN
        val carPlate = "testCarPlate"
        val testFine = Fine(
            id = "testFineId",
            car = Fine.Car(
                plate = carPlate,
                make = "testMake",
                model = "testModel",
                color = "testColor"
            ),
            trafficTickets = listOf()
        )

        every {
            fineRepository.getFineByCarPlate(carPlate)
        } returns Mono.just(testFine)

        // WHEN
        val result: Mono<Fine> = fineService.getFineByCarPlate(carPlate)

        // THEN
        StepVerifier.create(result)
            .expectNext(testFine)
            .verifyComplete()

        // THEN
        verify { fineRepository.getFineByCarPlate(carPlate) }
    }
}
