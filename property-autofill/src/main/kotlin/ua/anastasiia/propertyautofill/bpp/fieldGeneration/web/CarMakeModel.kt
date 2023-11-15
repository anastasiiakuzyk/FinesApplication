package ua.anastasiia.propertyautofill.bpp.fieldGeneration.web

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Suppress("ConstructorParameterNaming")
data class CarMakeModel(
    val year: Int,
    val id: Int,
    val horsepower: Int,
    val make: String,
    val model: String,
    val price: Int,
    val img_url: String,
)

fun getAllCarMakeModels(): List<CarMakeModel> {
    val client: Client = ClientBuilder.newClient()
    val response: Response = client.target("https://private-anon-8e43abed64-carsapi1.apiary-mock.com/cars")
        .request(MediaType.TEXT_PLAIN_TYPE)
        .get()

    val entity = response.readEntity(String::class.java)
    val mapper = jacksonObjectMapper()
    val cars: List<CarMakeModel> =
        mapper.readValue(entity, object : TypeReference<List<CarMakeModel>>() {})
    return cars
}
