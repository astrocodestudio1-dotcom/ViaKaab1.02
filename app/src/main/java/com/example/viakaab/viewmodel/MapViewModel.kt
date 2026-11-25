package com.example.viakaab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viakaab.data.CarrilloLandmark
import com.example.viakaab.data.GoogleMapsRepository
import com.example.viakaab.data.SessionRepository
import com.example.viakaab.model.DriverRideRequest
import com.example.viakaab.model.DriverTripStage
import com.example.viakaab.model.ServiceMode
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PassengerMapUiState(
    val selectedMode: ServiceMode = ServiceMode.STANDARD,
    val pickupText: String = "Mi ubicación actual (Centro)",
    val destinationText: String = "",
    val seatsText: String = "",
    val packageDetailsText: String = "",
    val recipientText: String = "",
    val fareOfferText: String = "",
    val assistanceNote: String = "",
    val userLatLng: LatLng = LatLng(19.5775, -88.0453),
    val destinationLatLng: LatLng? = null,
    val destinationMarkerTitle: String = "",
    val estimatedDistanceKm: Double? = null,
    val suggestedFareMxn: Int? = null,
    val toastMessage: String? = null
)

data class DriverMapUiState(
    val isOnline: Boolean = true,
    val tripStage: DriverTripStage = DriverTripStage.OFFER_AVAILABLE,
    val requestQueue: List<DriverRideRequest> = defaultRequests(),
    val currentRequestIndex: Int = 0,
    val completedTripsToday: Int = 0,
    val earningsTodayMxn: Int = 0,
    val toastMessage: String? = null
) {
    val activeRequest: DriverRideRequest
        get() = requestQueue.getOrElse(currentRequestIndex % requestQueue.size) { requestQueue.first() }
}

private fun defaultRequests(): List<DriverRideRequest> = listOf(
    DriverRideRequest(
        id = "req_1",
        origin = "Parque Principal Centro",
        destination = "TecNM Carrillo Puerto",
        fareMxn = 45,
        passengerName = "María G.",
        distanceKm = "1.8 km",
        startLat = 19.5775,
        startLng = -88.0453,
        endLat = 19.5850,
        endLng = -88.0410,
        serviceTypeLabel = "Viaje Estándar"
    ),
    DriverRideRequest(
        id = "req_2",
        origin = "Terminal ADO",
        destination = "Hospital General",
        fareMxn = 40,
        passengerName = "Carlos M.",
        distanceKm = "1.4 km",
        startLat = 19.5790,
        startLng = -88.0440,
        endLat = 19.5720,
        endLng = -88.0490,
        serviceTypeLabel = "Viaje Estándar"
    ),
    DriverRideRequest(
        id = "req_3",
        origin = "Mercado Municipal",
        destination = "Col. Emiliano Zapata",
        fareMxn = 35,
        passengerName = "Lucía P.",
        distanceKm = "1.1 km",
        startLat = 19.5760,
        startLng = -88.0430,
        endLat = 19.5830,
        endLng = -88.0380,
        serviceTypeLabel = "Entrega de Paquete"
    )
)

class MapViewModel(
    private val sessionRepository: SessionRepository,
    private val mapsRepository: GoogleMapsRepository
) : ViewModel() {

    val session = sessionRepository.session
    val quickLandmarks: List<CarrilloLandmark> = mapsRepository.quickLandmarks

    private val _passengerState = MutableStateFlow(PassengerMapUiState())
    val passengerState: StateFlow<PassengerMapUiState> = _passengerState.asStateFlow()

    private val _driverState = MutableStateFlow(DriverMapUiState())
    val driverState: StateFlow<DriverMapUiState> = _driverState.asStateFlow()

    private var geocodeJob: Job? = null

    fun applyEntryContext(mode: ServiceMode, assistanceNote: String = "") {
        _passengerState.update { state ->
            state.copy(
                selectedMode = mode,
                assistanceNote = assistanceNote,
                suggestedFareMxn = state.estimatedDistanceKm?.let { mapsRepository.suggestFareMxn(it, mode) }
            )
        }
    }

    fun setServiceMode(mode: ServiceMode) {
        val message = when (mode) {
            ServiceMode.DELIVERY -> "Modo: Entrega y Reparto de Paquetes seleccionado"
            ServiceMode.INTERCITY -> "Modo: Viaje Ciudad a Ciudad seleccionado"
            ServiceMode.STANDARD -> "Modo: Viaje Estándar seleccionado"
        }
        _passengerState.update { state ->
            state.copy(
                selectedMode = mode,
                suggestedFareMxn = state.estimatedDistanceKm?.let { mapsRepository.suggestFareMxn(it, mode) },
                toastMessage = message
            )
        }
    }

    fun updatePickup(value: String) {
        _passengerState.update { it.copy(pickupText = value) }
    }

    fun updateDestination(value: String) {
        _passengerState.update { it.copy(destinationText = value) }
        geocodeJob?.cancel()
        val trimmed = value.trim()
        if (trimmed.length >= 3) {
            geocodeJob = viewModelScope.launch {
                delay(350)
                val result = mapsRepository.geocodeAddress(trimmed)
                val destLatLng = LatLng(result.lat, result.lng)
                val origin = _passengerState.value.userLatLng
                val distKm = mapsRepository.calculateDistanceKm(origin, destLatLng)
                val suggested = mapsRepository.suggestFareMxn(distKm, _passengerState.value.selectedMode)
                _passengerState.update {
                    it.copy(
                        destinationLatLng = destLatLng,
                        destinationMarkerTitle = "Destino: ${result.formattedAddress}",
                        estimatedDistanceKm = distKm,
                        suggestedFareMxn = suggested,
                        fareOfferText = it.fareOfferText.ifBlank { suggested.toString() }
                    )
                }
            }
        } else if (trimmed.isEmpty()) {
            _passengerState.update {
                it.copy(
                    destinationLatLng = null,
                    destinationMarkerTitle = "",
                    estimatedDistanceKm = null,
                    suggestedFareMxn = null
                )
            }
        }
    }

    fun selectQuickLandmark(landmark: CarrilloLandmark) {
        geocodeJob?.cancel()
        val distKm = mapsRepository.calculateDistanceKm(_passengerState.value.userLatLng, landmark.latLng)
        val suggested = mapsRepository.suggestFareMxn(distKm, _passengerState.value.selectedMode)
        _passengerState.update {
            it.copy(
                destinationText = landmark.query,
                destinationLatLng = landmark.latLng,
                destinationMarkerTitle = "Destino: ${landmark.label}",
                estimatedDistanceKm = distKm,
                suggestedFareMxn = suggested,
                fareOfferText = suggested.toString()
            )
        }
    }

    fun updateSeats(value: String) {
        _passengerState.update { it.copy(seatsText = value) }
    }

    fun updatePackageDetails(value: String) {
        _passengerState.update { it.copy(packageDetailsText = value) }
    }

    fun updateRecipient(value: String) {
        _passengerState.update { it.copy(recipientText = value) }
    }

    fun updateFareOffer(value: String) {
        _passengerState.update { it.copy(fareOfferText = value) }
    }

    fun onUserLocationResolved(lat: Double, lng: Double) {
        val latLng = LatLng(lat, lng)
        _passengerState.update { state ->
            val dist = state.destinationLatLng?.let { mapsRepository.calculateDistanceKm(latLng, it) }
            state.copy(
                userLatLng = latLng,
                estimatedDistanceKm = dist,
                suggestedFareMxn = dist?.let { mapsRepository.suggestFareMxn(it, state.selectedMode) }
            )
        }
        viewModelScope.launch {
            val address = mapsRepository.reverseGeocode(lat, lng)
            _passengerState.update { it.copy(pickupText = address) }
        }
    }

    fun onMapTapped(latLng: LatLng) {
        val origin = _passengerState.value.userLatLng
        val distKm = mapsRepository.calculateDistanceKm(origin, latLng)
        val suggested = mapsRepository.suggestFareMxn(distKm, _passengerState.value.selectedMode)
        _passengerState.update {
            it.copy(
                destinationLatLng = latLng,
                destinationMarkerTitle = "Punto de referencia seleccionado",
                estimatedDistanceKm = distKm,
                suggestedFareMxn = suggested,
                fareOfferText = it.fareOfferText.ifBlank { suggested.toString() }
            )
        }
        viewModelScope.launch {
            val address = mapsRepository.reverseGeocode(latLng.latitude, latLng.longitude)
            _passengerState.update { it.copy(destinationText = address) }
        }
    }

    fun publishTripOffer() {
        val state = _passengerState.value
        val originName = state.pickupText.ifBlank { "Centro" }
        sessionRepository.addTripRequest(
            origin = originName,
            destination = state.destinationText,
            fareMxn = state.fareOfferText,
            mode = state.selectedMode
        )

        val destLatLng = state.destinationLatLng ?: LatLng(19.5850, -88.0410)
        val parsedFare = state.fareOfferText.toIntOrNull() ?: (state.suggestedFareMxn ?: 45)
        val passengerName = sessionRepository.session.value.effectiveDisplayName
        val distLabel = state.estimatedDistanceKm?.let { "${"%.1f".format(it)} km" } ?: "2.0 km"
        val newDriverRequest = DriverRideRequest(
            id = "req_${System.currentTimeMillis()}",
            origin = originName,
            destination = state.destinationText,
            fareMxn = parsedFare,
            passengerName = passengerName,
            distanceKm = distLabel,
            startLat = state.userLatLng.latitude,
            startLng = state.userLatLng.longitude,
            endLat = destLatLng.latitude,
            endLng = destLatLng.longitude,
            serviceTypeLabel = state.selectedMode.label
        )

        _driverState.update { dState ->
            dState.copy(
                requestQueue = listOf(newDriverRequest) + dState.requestQueue,
                currentRequestIndex = 0,
                tripStage = DriverTripStage.OFFER_AVAILABLE
            )
        }

        _passengerState.update {
            it.copy(
                destinationText = "",
                fareOfferText = "",
                packageDetailsText = "",
                recipientText = "",
                seatsText = "",
                assistanceNote = "",
                destinationLatLng = null,
                destinationMarkerTitle = "",
                estimatedDistanceKm = null,
                suggestedFareMxn = null,
                toastMessage = "¡Oferta enviada a conductores y repartidores cerca de ti!"
            )
        }
    }

    fun clearPassengerToast() {
        _passengerState.update { it.copy(toastMessage = null) }
    }

    fun toggleDriverOnline(isOnline: Boolean) {
        val msg = if (isOnline) {
            "Estás en línea recibiendo solicitudes cerca de ti"
        } else {
            "Modo fuera de servicio"
        }
        _driverState.update { it.copy(isOnline = isOnline, toastMessage = msg) }
    }

    fun sendCounterOffer(amount: String) {
        val parsed = amount.toIntOrNull() ?: return
        _driverState.update { state ->
            val updatedQueue = state.requestQueue.mapIndexed { idx, req ->
                if (idx == state.currentRequestIndex % state.requestQueue.size) {
                    req.copy(fareMxn = parsed)
                } else {
                    req
                }
            }
            state.copy(
                requestQueue = updatedQueue,
                toastMessage = "Contraoferta de $$parsed MXN enviada al pasajero"
            )
        }
    }

    fun skipDriverRequest() {
        _driverState.update { state ->
            state.copy(
                currentRequestIndex = (state.currentRequestIndex + 1) % state.requestQueue.size,
                tripStage = DriverTripStage.OFFER_AVAILABLE,
                toastMessage = "Mostrando siguiente solicitud disponible"
            )
        }
    }

    fun startDriverRoute() {
        _driverState.update {
            it.copy(
                tripStage = DriverTripStage.HEADING_TO_PICKUP,
                toastMessage = "Ruta iniciada hacia el pasajero"
            )
        }
    }

    fun advanceDriverTripStage() {
        _driverState.update { state ->
            when (state.tripStage) {
                DriverTripStage.OFFER_AVAILABLE -> state.copy(
                    tripStage = DriverTripStage.HEADING_TO_PICKUP,
                    toastMessage = "En camino al punto de recogida"
                )
                DriverTripStage.HEADING_TO_PICKUP -> state.copy(
                    tripStage = DriverTripStage.TRIP_IN_PROGRESS,
                    toastMessage = "¡Pasajero a bordo! Viaje en curso"
                )
                DriverTripStage.TRIP_IN_PROGRESS -> {
                    val fare = state.activeRequest.fareMxn
                    val req = state.activeRequest
                    sessionRepository.addTripRequest(
                        origin = req.origin,
                        destination = req.destination,
                        fareMxn = "$fare",
                        mode = ServiceMode.STANDARD
                    )
                    state.copy(
                        tripStage = DriverTripStage.OFFER_AVAILABLE,
                        currentRequestIndex = (state.currentRequestIndex + 1) % state.requestQueue.size,
                        completedTripsToday = state.completedTripsToday + 1,
                        earningsTodayMxn = state.earningsTodayMxn + fare,
                        toastMessage = "¡Viaje finalizado! +$$fare MXN registrados"
                    )
                }
            }
        }
    }

    fun clearDriverToast() {
        _driverState.update { it.copy(toastMessage = null) }
    }
}
