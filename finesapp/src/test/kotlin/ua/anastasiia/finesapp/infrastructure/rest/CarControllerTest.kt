package ua.anastasiia.finesapp.infrastructure.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import reactor.kotlin.core.publisher.toMono
import ua.anastasiia.finesapp.application.exception.CarPlateNotFoundException
import ua.anastasiia.finesapp.application.port.input.FineServiceInPort
import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.infrastructure.rest.mapper.toResponse

@WebMvcTest
@ActiveProfiles("test")
class CarControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var fineService: FineServiceInPort

    @Test
    fun `should return sum when sum endpoint`() {
        // GIVEN
        every { fineService.getSumOfFinesForCarPlate(any()) } returns 23.0.toMono()

        // WHEN
        val mvcResult: MvcResult = mockMvc.perform(get("/cars/sum/car/23"))
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(23.0))
            .andReturn()

        // THEN
        this.mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(content().string("23.0"))
    }

    @Test
    fun `should return fine for car plate`() {
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
            fineService.getFineByCarPlate(any())
        } returns
                fine.toMono()

        // WHEN
        val mvcResult = mockMvc.perform(get("/cars/plate/AA1234BB"))
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(fine.toResponse()))
            .andReturn()

        // THEN
        this.mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(
                content().string(
                    "{\"id\":\"testFineId\"," +
                            "\"car\":{" +
                            "\"plate\":\"AA1234BB\"," +
                            "\"make\":\"testMake\"," +
                            "\"model\":\"testModel\"," +
                            "\"color\":\"testColor\"}," +
                            "\"trafficTickets\":[]}"
                )
            )
    }

    @Test
    fun `should throw exception when no car found by plate`() {
        // GIVEN
        every {
            fineService.getFineByCarPlate(any())
        } returns CarPlateNotFoundException("nonExistentPlate").toMono()

        // WHEN
        val mvcResult = mockMvc.perform(get("/cars/plate/nonExistentPlate"))
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andExpect { result ->
                assertEquals(0, result.response.contentLength)
            }
            .andReturn()

        // THEN
        runCatching {
            mockMvc.perform(
                asyncDispatch(mvcResult)
            )
        }.onFailure {
            assertEquals(
                "Request processing failed: " +
                        "ua.anastasiia.finesapp.application.exception.CarPlateNotFoundException: " +
                        "Car with plate nonExistentPlate not found",
                it.message
            )
        }
    }
}
