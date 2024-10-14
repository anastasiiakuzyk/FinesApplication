package ua.anastasiia.finesapp.utils

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.Serializable
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.springframework.core.io.DefaultResourceLoader
import kotlin.random.Random

fun generatePointsOnRoad(number: Int): List<Pair<Double, Double>> {
    val randomPoints: List<Pair<Double, Double>> = generateRandomPointInPolygon(number)
    return getNearestRoadPoint(randomPoints)
}

private fun generateRandomPointInPolygon(number: Int): List<Pair<Double, Double>> {
    val geometryFactory = GeometryFactory()
    val resource = DefaultResourceLoader().getResource("kyiv_polygon.json")
    val geoJsonResponses = jacksonObjectMapper()
        .readValue(resource.url, object : TypeReference<List<GeoJson>>() {})

    val coordinates = geoJsonResponses[0].geojson.coordinates[0].map { Coordinate(it[0], it[1]) }.toTypedArray()
    val polygonGeometry: Polygon = geometryFactory.createPolygon(coordinates)

    val minLon = coordinates.minOf { it.x }
    val maxLon = coordinates.maxOf { it.x }
    val minLat = coordinates.minOf { it.y }
    val maxLat = coordinates.maxOf { it.y }

    return generateSequence {
        val randomLat = Random.nextDouble(minLat, maxLat)
        val randomLon = Random.nextDouble(minLon, maxLon)
        randomLat to randomLon
    }.filter { (lat, lon) ->
        val point = geometryFactory.createPoint(Coordinate(lon, lat))
        polygonGeometry.contains(point)
    }.take(number).toList()
}

@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GeoJson(
    val geojson: Polygon
) {
    @Serializable
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Polygon(
        val coordinates: List<List<List<Double>>>
    )
}
