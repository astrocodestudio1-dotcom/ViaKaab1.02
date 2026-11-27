package com.example.viakaab.data

import com.example.viakaab.BuildConfig
import com.example.viakaab.model.NavigationStep
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.model.StreetNavigationRoute
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class GeocodeResult(
    val lat: Double,
    val lng: Double,
    val formattedAddress: String
)

data class CarrilloLandmark(
    val label: String,
    val query: String,
    val latLng: LatLng
)

class GoogleMapsRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    val quickLandmarks = listOf(
        CarrilloLandmark("Centro", "Centro, Felipe Carrillo Puerto", LatLng(19.5775, -88.0453)),
        CarrilloLandmark("TecNM", "TecNM Campus Felipe Carrillo Puerto", LatLng(19.5850, -88.0410)),
        CarrilloLandmark("Hospital", "Hospital General Felipe Carrillo Puerto", LatLng(19.5720, -88.0490)),
        CarrilloLandmark("Mercado", "Mercado Municipal Felipe Carrillo Puerto", LatLng(19.5760, -88.0430)),
        CarrilloLandmark("Terminal ADO", "Terminal ADO Felipe Carrillo Puerto", LatLng(19.5790, -88.0440)),
        CarrilloLandmark("Tulum", "Tulum, Quintana Roo", LatLng(20.2114, -87.4654)),
        CarrilloLandmark("Chetumal", "Chetumal, Quintana Roo", LatLng(18.5141, -88.3038))
    )

    suspend fun geocodeAddress(query: String): GeocodeResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isNotBlank() && !apiKey.startsWith("YOUR_")) {
            try {
                val encodedQuery = URLEncoder.encode("$query, Quintana Roo, México", "UTF-8")
                val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedQuery&key=$apiKey"
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val results = json.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        val first = results.getJSONObject(0)
                        val formatted = first.optString("formatted_address", query)
                        val location = first.getJSONObject("geometry").getJSONObject("location")
                        return@withContext GeocodeResult(
                            lat = location.getDouble("lat"),
                            lng = location.getDouble("lng"),
                            formattedAddress = formatted
                        )
                    }
                }
            } catch (_: Exception) {
                // Fall back to local Felipe Carrillo Puerto landmarks
            }
        }
        val fallback = getCoordinatesForLandmark(query.lowercase())
        GeocodeResult(fallback.latitude, fallback.longitude, query)
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isNotBlank() && !apiKey.startsWith("YOUR_")) {
            try {
                val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$lng&key=$apiKey"
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val results = json.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        return@withContext results.getJSONObject(0)
                            .optString("formatted_address", "Ubicación GPS")
                    }
                }
            } catch (_: Exception) {
                // Fall back gracefully
            }
        }
        val nearest = quickLandmarks.minByOrNull { landmark ->
            calculateDistanceKm(LatLng(lat, lng), landmark.latLng)
        }
        if (nearest != null && calculateDistanceKm(LatLng(lat, lng), nearest.latLng) < 1.2) {
            "Cerca de ${nearest.label} (Felipe Carrillo Puerto)"
        } else {
            "Ubicación seleccionada (${"%.4f".format(lat)}, ${"%.4f".format(lng)})"
        }
    }

    fun getCoordinatesForLandmark(query: String): LatLng {
        return when {
            "tecnm" in query || "tecnologico" in query || "tecnológico" in query -> LatLng(19.5850, -88.0410)
            "hospital" in query -> LatLng(19.5720, -88.0490)
            "mercado" in query -> LatLng(19.5760, -88.0430)
            "zapata" in query || "emiliano" in query -> LatLng(19.5830, -88.0380)
            "cbtis" in query || "prepa" in query -> LatLng(19.5810, -88.0480)
            "ado" in query || "terminal" in query -> LatLng(19.5790, -88.0440)
            "chetumal" in query -> LatLng(18.5141, -88.3038)
            "tulum" in query -> LatLng(20.2114, -87.4654)
            "playa" in query -> LatLng(20.6296, -87.0739)
            "cancun" in query || "cancún" in query -> LatLng(21.1619, -86.8515)
            "centro" in query -> LatLng(19.5775, -88.0453)
            else -> LatLng(19.5820, -88.0420)
        }
    }

    fun calculateDistanceKm(from: LatLng, to: LatLng): Double {
        val r = 6371.0
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun suggestFareMxn(distanceKm: Double, mode: ServiceMode): Int {
        val raw = when (mode) {
            ServiceMode.STANDARD -> 20.0 + distanceKm * 8.0
            ServiceMode.DELIVERY -> 25.0 + distanceKm * 9.0
            ServiceMode.INTERCITY -> 80.0 + distanceKm * 8.5
        }
        return ((raw / 5.0).roundToInt() * 5).coerceAtLeast(25)
    }

    fun calculateDistanceMeters(from: LatLng, to: LatLng): Double =
        calculateDistanceKm(from, to) * 1000.0

    fun distancePointToRouteMeters(point: LatLng, route: List<LatLng>): Double {
        if (route.isEmpty()) return Double.MAX_VALUE
        if (route.size == 1) return calculateDistanceMeters(point, route.first())
        val latFactor = 111_132.0
        val lngFactor = 111_320.0 * cos(Math.toRadians(point.latitude)).coerceAtLeast(0.1)
        var best = Double.MAX_VALUE
        for (i in 0 until route.lastIndex) {
            val a = route[i]
            val b = route[i + 1]
            val ax = (a.longitude - point.longitude) * lngFactor
            val ay = (a.latitude - point.latitude) * latFactor
            val bx = (b.longitude - point.longitude) * lngFactor
            val by = (b.latitude - point.latitude) * latFactor
            val dx = bx - ax
            val dy = by - ay
            val lenSq = dx * dx + dy * dy
            val t = if (lenSq > 1e-6) (-(ax * dx + ay * dy) / lenSq).coerceIn(0.0, 1.0) else 0.0
            val px = ax + t * dx
            val py = ay + t * dy
            val d = hypot(px, py)
            if (d < best) best = d
        }
        return best
    }

    suspend fun fetchStreetRoute(origin: LatLng, destination: LatLng): StreetNavigationRoute =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.MAPS_API_KEY.trim()
            if (apiKey.isNotEmpty() && !apiKey.startsWith("YOUR_", ignoreCase = true)) {
                fetchGoogleDirectionsRoute(origin, destination, apiKey)?.let { return@withContext it }
            }
            fetchOsrmRoute(origin, destination)?.let { return@withContext it }

            val distMeters = calculateDistanceMeters(origin, destination).roundToInt().coerceAtLeast(100)
            val durationSec = ((distMeters / 1000.0) * 165.0).roundToInt().coerceAtLeast(60)
            StreetNavigationRoute(
                points = listOf(origin, destination),
                steps = listOf(
                    NavigationStep(
                        instruction = "Continúa hacia tu destino",
                        streetName = "Felipe Carrillo Puerto",
                        distanceMeters = distMeters,
                        durationSeconds = durationSec,
                        maneuver = "straight",
                        endLat = destination.latitude,
                        endLng = destination.longitude
                    )
                ),
                totalDistanceMeters = distMeters,
                totalDurationSeconds = durationSec,
                sourceLabel = "GPS directo"
            )
        }

    private fun fetchGoogleDirectionsRoute(
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ): StreetNavigationRoute? {
        return runCatching {
            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=driving&language=es&key=$apiKey"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                if (json.optString("status") != "OK") return null
                val routes = json.optJSONArray("routes") ?: return null
                if (routes.length() == 0) return null
                val firstRoute = routes.getJSONObject(0)
                val encodedOverview = firstRoute
                    .optJSONObject("overview_polyline")
                    ?.optString("points")
                    .orEmpty()
                val legs = firstRoute.optJSONArray("legs") ?: return null
                if (legs.length() == 0) return null
                val firstLeg = legs.getJSONObject(0)
                val totalDistanceMeters = firstLeg.optJSONObject("distance")?.optInt("value") ?: 0
                val totalDurationSeconds = firstLeg.optJSONObject("duration")?.optInt("value") ?: 0
                val stepsJson = firstLeg.optJSONArray("steps")
                val steps = mutableListOf<NavigationStep>()
                val detailedPoints = mutableListOf<LatLng>()

                if (stepsJson != null) {
                    for (i in 0 until stepsJson.length()) {
                        val s = stepsJson.getJSONObject(i)
                        val rawHtml = s.optString("html_instructions", "Continúa por la vía")
                        val cleanInstruction = rawHtml
                            .replace(Regex("<[^>]*>"), " ")
                            .replace("&nbsp;", " ")
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        val distM = s.optJSONObject("distance")?.optInt("value") ?: 0
                        val durS = s.optJSONObject("duration")?.optInt("value") ?: 0
                        val maneuver = s.optString("maneuver", "straight")
                        val endLoc = s.optJSONObject("end_location")
                        val endLat = endLoc?.optDouble("lat") ?: destination.latitude
                        val endLng = endLoc?.optDouble("lng") ?: destination.longitude
                        val stepPoly = s.optJSONObject("polyline")?.optString("points").orEmpty()
                        if (stepPoly.isNotBlank()) {
                            detailedPoints.addAll(decodePolyline(stepPoly))
                        }
                        steps.add(
                            NavigationStep(
                                instruction = cleanInstruction,
                                streetName = cleanInstruction,
                                distanceMeters = distM,
                                durationSeconds = durS,
                                maneuver = maneuver,
                                endLat = endLat,
                                endLng = endLng
                            )
                        )
                    }
                }

                val polyline = if (detailedPoints.size >= 2) {
                    detailedPoints
                } else {
                    decodePolyline(encodedOverview)
                }
                if (polyline.size < 2) return null
                StreetNavigationRoute(
                    points = polyline,
                    steps = steps,
                    totalDistanceMeters = totalDistanceMeters.coerceAtLeast(100),
                    totalDurationSeconds = totalDurationSeconds.coerceAtLeast(60),
                    sourceLabel = "Google Maps"
                )
            }
        }.getOrNull()
    }

    private fun fetchOsrmRoute(origin: LatLng, destination: LatLng): StreetNavigationRoute? {
        return runCatching {
            val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}" +
                "?overview=full&geometries=polyline&steps=true"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ViaKaabAndroid/1.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                if (json.optString("code") != "Ok") return null
                val routes = json.optJSONArray("routes") ?: return null
                if (routes.length() == 0) return null
                val route = routes.getJSONObject(0)
                val encoded = route.optString("geometry")
                val points = decodePolyline(encoded)
                if (points.size < 2) return null
                val totalDistanceMeters = route.optDouble("distance", 0.0).roundToInt().coerceAtLeast(100)
                val totalDurationSeconds = route.optDouble("duration", 0.0).roundToInt().coerceAtLeast(60)

                val steps = mutableListOf<NavigationStep>()
                val legs = route.optJSONArray("legs")
                if (legs != null && legs.length() > 0) {
                    val stepsArr = legs.getJSONObject(0).optJSONArray("steps")
                    if (stepsArr != null) {
                        for (i in 0 until stepsArr.length()) {
                            val st = stepsArr.getJSONObject(i)
                            val streetName = st.optString("name", "").trim()
                            val distM = st.optDouble("distance", 0.0).roundToInt()
                            val durS = st.optDouble("duration", 0.0).roundToInt()
                            val maneuverObj = st.optJSONObject("maneuver")
                            val type = maneuverObj?.optString("type", "") ?: ""
                            val modifier = maneuverObj?.optString("modifier", "") ?: ""
                            val locArr = maneuverObj?.optJSONArray("location")
                            val endLng = locArr?.optDouble(0) ?: destination.longitude
                            val endLat = locArr?.optDouble(1) ?: destination.latitude
                            val instruction = formatOsrmSpanishInstruction(type, modifier, streetName)
                            val normalizedManeuver = normalizeOsrmManeuver(type, modifier)
                            steps.add(
                                NavigationStep(
                                    instruction = instruction,
                                    streetName = streetName.ifBlank { "Calle local" },
                                    distanceMeters = distM,
                                    durationSeconds = durS,
                                    maneuver = normalizedManeuver,
                                    endLat = endLat,
                                    endLng = endLng
                                )
                            )
                        }
                    }
                }

                StreetNavigationRoute(
                    points = points,
                    steps = steps,
                    totalDistanceMeters = totalDistanceMeters,
                    totalDurationSeconds = totalDurationSeconds,
                    sourceLabel = "Calles FCP"
                )
            }
        }.getOrNull()
    }

    private fun normalizeOsrmManeuver(type: String, modifier: String): String {
        return when {
            type == "arrive" -> "arrive"
            type == "roundabout" || type == "rotary" -> "roundabout-right"
            "uturn" in modifier -> "uturn-left"
            "sharp left" in modifier || "left" == modifier -> "turn-left"
            "slight left" in modifier -> "turn-slight-left"
            "sharp right" in modifier || "right" == modifier -> "turn-right"
            "slight right" in modifier -> "turn-slight-right"
            else -> "straight"
        }
    }

    private fun formatOsrmSpanishInstruction(
        type: String,
        modifier: String,
        streetName: String
    ): String {
        val onto = if (streetName.isNotBlank()) " por $streetName" else ""
        return when (type) {
            "depart" -> if (streetName.isNotBlank()) "Toma $streetName" else "Inicia el recorrido"
            "arrive" -> "Has llegado al punto indicado"
            "roundabout", "rotary" -> "Toma la glorieta$onto"
            "new name", "continue" -> "Continúa$onto"
            else -> when (modifier) {
                "left" -> "Gira a la izquierda$onto"
                "slight left" -> "Mantente a la izquierda$onto"
                "sharp left" -> "Gira cerrado a la izquierda$onto"
                "right" -> "Gira a la derecha$onto"
                "slight right" -> "Mantente a la derecha$onto"
                "sharp right" -> "Gira cerrado a la derecha$onto"
                "uturn" -> "Da vuelta en U$onto"
                else -> "Continúa recto$onto"
            }
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        if (encoded.isBlank()) return emptyList()
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20 && index < len)
            val dLat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20 && index < len)
            val dLng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dLng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
}
