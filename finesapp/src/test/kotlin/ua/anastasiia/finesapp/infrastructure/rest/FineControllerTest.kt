package ua.anastasiia.finesapp.infrastructure.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import reactor.kotlin.core.publisher.toFlux
import reactor.test.StepVerifier
import ua.anastasiia.finesapp.application.port.input.FineServiceInPort
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.rest.dto.response.FineResponse


@WebMvcTest
@ActiveProfiles("test")
class FineControllerTest {

    @MockkBean
    lateinit var fineService: FineServiceInPort

    @Test
    fun `should return all fines when fines endpoint`() {
        // GIVEN
        val fine = Fine(
            id = "testFineId",
            car = Fine.Car(
                plate = "AA1234BB",
                make = "testMake",
                model = "testModel",
                color = "testColor"
            ),
            trafficTickets = listOf()
        )
        every {
            fineService.getAllFines()
        } returns listOf(fine).toFlux()

        // WHEN
        val client = MockMvcWebTestClient.bindToController(FineController(fineService)).build()
        val exchangeResult: FluxExchangeResult<FineResponse> = client.get()
            .uri("/fines")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("text/event-stream")
            .returnResult(FineResponse::class.java)

        // THEN
        StepVerifier.create(exchangeResult.responseBody)
            .consumeNextWith {
                assertThat(
                    it.id
                ).isEqualTo("testFineId")
            }.verifyComplete()

        // AND
        verify { fineService.getAllFines() }
    }

}
