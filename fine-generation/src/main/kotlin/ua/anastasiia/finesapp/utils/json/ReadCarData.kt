package ua.anastasiia.finesapp.utils.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.Serializable
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource

@Serializable
data class CarData(
    val plateNumber: String? = null,
    val markName: String? = null,
    val modelName: String? = null,
    val color: ColorData,
    val addDate: String,
    val photoData: PhotoData,
    val autoData: AutoData
) {
    @Serializable
    data class ColorData(
        val eng: String? = null
    )

    @Serializable
    data class PhotoData(
        val seoLinkF: String
    )

    @Serializable
    data class AutoData(
        val categoryId: Int? = null,
        val bodyId: Int? = null
    )
}

fun readCarDataFromJson(
    inputFileName: String
): MutableList<CarData> {
    val inputResource: Resource = DefaultResourceLoader().getResource(inputFileName)
    return jacksonObjectMapper().readValue(inputResource.url, object : TypeReference<MutableList<CarData>>() {})
}
