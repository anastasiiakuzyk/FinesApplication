package ua.anastasiia.finesapp.web

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import ua.anastasiia.finesapp.utils.json.CarData
import java.io.File

const val apiKey: String = "E9JvBwLkFoeft6MhlWyizDjRK7FCed31bqPGueQP"
const val getCarInfoUrl: String = "https://developers.ria.com/auto/info"
const val getCarsUrl: String = "https://developers.ria.com/auto/search"
val okHttpClient: OkHttpClient = OkHttpClient()

@Serializable
data class AllIdsResponse(
    val result: Result
) {
    @Serializable
    data class Result(
        val search_result: SearchResult
    ) {
        @Serializable
        data class SearchResult(
            val count: Int,
            val ids: List<String>
        )
    }
}

fun getCarInfo(id: String): CarData? {
    val url = getCarInfoUrl.toHttpUrlOrNull()?.newBuilder()
        ?.addQueryParameter("api_key", apiKey)
        ?.addQueryParameter("auto_id", id)
        ?.build()

    val request = Request.Builder()
        .url(url!!)
        .build()

    var carData: CarData? = null
    okHttpClient.newCall(request).execute().use { response ->
        if (response.code != 200) {
            return null
        }
        if (response.isSuccessful) {
            response.body?.string()?.let {
                carData = Json { ignoreUnknownKeys = true }.decodeFromString<CarData>(it)
            }
        }
    }
    return carData
}

fun getCarIdsBatch(page: Int): AllIdsResponse? {
    val url = getCarsUrl.toHttpUrlOrNull()?.newBuilder()
        ?.addQueryParameter("api_key", apiKey)
        ?.addQueryParameter("page", page.toString())
        ?.build()

    val request = Request.Builder()
        .url(url!!)
        .build()

    var allIdsResponse: AllIdsResponse? = null
    okHttpClient.newCall(request).execute().use { response ->
        if (response.code != 200) {
            return null
        }
        if (response.isSuccessful) {
            response.body?.string()?.let {
                allIdsResponse = Json { ignoreUnknownKeys = true }.decodeFromString<AllIdsResponse>(it)
            }
        }
    }
    return allIdsResponse
}

fun writeToJson(
    outputFileName: String,
    carData: List<CarData?>
) {
    val outputFile = File(outputFileName)
    outputFile.createNewFile()
    val json = Json { prettyPrint = true }
    carData.joinToString(
        postfix = ",",
        separator = ",",
    ) { hubConnection ->
        json.encodeToString(hubConnection)
    }.let { outputFile.appendText(it) }
}

private fun fetchCarData(numberOfPages: Int) {
    val carData: MutableList<CarData?> = mutableListOf()
    repeat(numberOfPages) {
        val page = it + 160
        val carIdsBatch: AllIdsResponse? = getCarIdsBatch(page)
        if (carIdsBatch == null) {
            writeToJson("cars_data.json", carData)
            return
        } else {
            val ids: List<String> = carIdsBatch.result.search_result.ids
            ids.forEachIndexed { index, id ->
                println("Batch[$page]: [$index] [$id]")
                carData.add(getCarInfo(id))
            }
        }
    }

    writeToJson("cars_data.json", carData)
}
