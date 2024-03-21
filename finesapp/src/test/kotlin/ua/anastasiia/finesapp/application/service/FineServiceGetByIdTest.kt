package ua.anastasiia.finesapp.application.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import ua.anastasiia.finesapp.application.exception.FineIdNotFoundException
import ua.anastasiia.finesapp.application.port.output.FineRepositoryOutPort
import ua.anastasiia.finesapp.application.port.output.TrafficTicketAddedEventProducerOutPort
import ua.anastasiia.finesapp.domain.Fine
import java.time.LocalDateTime

class FineServiceGetByIdTest {

    private val fineRepository: FineRepositoryOutPort = mockk()
    private val fineKafkaProducer: TrafficTicketAddedEventProducerOutPort = mockk()
    private val fineService: FineService = FineService(fineRepository, fineKafkaProducer)

    @Test
    fun `should return fine when fine found by id`() {
        // GIVEN
        val fineId = "testFineId"
        val testFine = Fine(
            id = fineId,
            car = Fine.Car(
                plate = "testPlate",
                make = "testMake",
                model = "testModel",
                color = "testColor"
            ),
            trafficTickets = listOf(
                Fine.TrafficTicket(
                    id = "testTrafficTicketId",
                    locationLat = 0.0,
                    locationLon = 0.0,
                    dateTime = LocalDateTime.now(),
                    photoUrl = "testPhotoUrl",
                    violations = listOf(
                        Fine.TrafficTicket.Violation(
                            description = "testViolationDescription",
                            price = 0.0
                        )
                    )
                )
            )
        )

        every {
            fineRepository.getFineById(fineId)
        } returns Mono.just(testFine)

        // WHEN
        val result: Mono<Fine> = fineService.getFineById(fineId)

        // THEN
        StepVerifier.create(result)
            .expectNext(testFine)
            .verifyComplete()

        // AND
        verify(exactly = 1) { fineRepository.getFineById(fineId) }
    }

    @Test
    fun `should throw exception when no fine found by id`() {
        // GIVEN
        val fineId = "nonExistentFineId"
        every {
            fineRepository.getFineById(fineId)
        } returns Mono.empty()

        // WHEN
        val result = fineService.getFineById(fineId)

        // THEN
        StepVerifier.create(result)
            .expectError(FineIdNotFoundException::class.java)
            .verify()

        // AND
        verify(exactly = 1) { fineRepository.getFineById(fineId) }
    }

}
