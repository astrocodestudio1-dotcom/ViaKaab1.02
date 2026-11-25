package com.example.viakaab.data

import com.example.viakaab.BuildConfig
import com.example.viakaab.model.ServiceMode
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import kotlin.math.atan2
import kotlin.math.cos
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

    private val client = OkHttpClient()

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
            ServiceMode.STANDARD -> 35.0 + distanceKm * 10.0
            ServiceMode.DELIVERY -> 30.0 + distanceKm * 9.0
            ServiceMode.INTERCITY -> 90.0 + distanceKm * 8.5
        }
        return ((raw / 5.0).roundToInt() * 5).coerceAtLeast(35)
    }
}
