package ua.anastasiia.finesapp.utils

import com.graphhopper.GraphHopper
import com.graphhopper.config.Profile
import com.graphhopper.routing.util.EdgeFilter
import com.graphhopper.storage.index.Snap
import com.graphhopper.util.GHUtility
import com.graphhopper.util.shapes.GHPoint
import org.springframework.core.io.DefaultResourceLoader

fun getNearestRoadPoint(points: List<Pair<Double, Double>>): List<Pair<Double, Double>> {
    val resource = DefaultResourceLoader().getResource("kyiv.pbf")
    val hopper = createGraphHopperInstance(resource.file.path)

    val roadPoints = points.map { (lat, lon) ->
        val randomPoint = GHPoint(lat, lon)
        val nearestPoint = findNearestRoadPoint(hopper, randomPoint)
        nearestPoint?.let {
            it.snappedPoint.lat to it.snappedPoint.lon
        } ?: (lat to lon)
    }

    hopper.close()
    return roadPoints
}


private fun createGraphHopperInstance(ghLoc: String): GraphHopper {
    return GraphHopper().apply {
        osmFile = ghLoc
        graphHopperLocation = "fine-generation/src/main/resources/target/road-graph-cache"
        encodedValuesString = "car_access, car_average_speed"
        setProfiles(Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car.json")))
        importOrLoad()
    }
}

private fun findNearestRoadPoint(hopper: GraphHopper, point: GHPoint): Snap? {
    val locationIndex = hopper.locationIndex
    val snap = locationIndex.findClosest(point.lat, point.lon, EdgeFilter.ALL_EDGES)
    return if (snap.isValid) snap else null
}
