package ua.anastasiia.finesapp.utils.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource

@JsonIgnoreProperties(ignoreUnknown = true)
data class MakeModel(
    val make: String,
    val model: String,
    @JsonProperty("vclass") val vehicleClass: String
)

fun readMakeModelFromJson(
    inputFileName: String
): List<MakeModel> {
    val inputResource: Resource = DefaultResourceLoader().getResource(inputFileName)
    return jacksonObjectMapper().readValue(inputResource.url, object : TypeReference<List<MakeModel>>() {})
}

