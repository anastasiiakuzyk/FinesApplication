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
    val color: ColorData? = null,
    val addDate: String? = null,
    val photoData: PhotoData? = null,
    val autoData: AutoData? = null
) {
    @Serializable
    data class ColorData(
        val eng: String? = null
    )

    @Serializable
    data class PhotoData(
        val seoLinkF: String?
    )

    @Serializable
    data class AutoData(
        val categoryId: Int? = null,
        val bodyId: Int? = null
    )
}

fun readCarDataFromJson(
    inputFileName: String
): List<CarData> {
    val inputResource: Resource = DefaultResourceLoader().getResource(inputFileName)
    val carData =
        jacksonObjectMapper().readValue(inputResource.url, object : TypeReference<MutableList<CarData>>() {})

    return carData.asSequence()
        .filter { it.plateNumber != null }
        .filter { it.markName != null }
        .filter { it.modelName != null }
        .filter { it.color != null }
        .filter { it.color!!.eng != null }
        .filter { it.photoData != null }
        .filter { it.photoData!!.seoLinkF != null }
        .filter { it.autoData != null }
        .filter { it.autoData!!.categoryId != null }
        .filter { it.autoData!!.bodyId != null }
        .distinctBy { it.plateNumber }
        .toList()
}
