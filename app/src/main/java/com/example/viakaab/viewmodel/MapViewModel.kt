package com.example.viakaab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viakaab.data.CarrilloLandmark
import com.example.viakaab.data.GoogleMapsRepository
import com.example.viakaab.data.SessionRepository
import com.example.viakaab.model.ActiveLiveTrip
import com.example.viakaab.model.CartItem
import com.example.viakaab.model.DriverFeedFilter
import com.example.viakaab.model.DriverRequestCategory
import com.example.viakaab.model.DriverRideRequest
import com.example.viakaab.model.DriverTripStage
import com.example.viakaab.model.EatsCategory
import com.example.viakaab.model.LiveTripPhase
import com.example.viakaab.model.LocalCommerce
import com.example.viakaab.model.MapVehicleType
import com.example.viakaab.model.OnlineMapUnit
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.model.UserRole
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class PassengerMapUiState(
    val selectedMode: ServiceMode = ServiceMode.STANDARD,
    val pickupText: String = "Ubicación actual en tiempo real (GPS)",
    val isUsingRealTimeLocation: Boolean = true,
    val hasGpsFix: Boolean = false,
    val lastGpsUpdatedLabel: String = "Esperando señal GPS",
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
    val activeLiveTrip: ActiveLiveTrip? = null,
    val isLiveTripPanelExpanded: Boolean = false,
    val toastMessage: String? = null
)

data class DriverMapUiState(
    val isOnline: Boolean = true,
    val tripStage: DriverTripStage = DriverTripStage.OFFER_AVAILABLE,
    val requestQueue: List<DriverRideRequest> = defaultRequests(),
    val selectedFeedFilter: DriverFeedFilter = DriverFeedFilter.ALL,
    val selectedRequestId: String = "",
    val currentRequestIndex: Int = 0,
    val completedTripsToday: Int = 0,
    val earningsTodayMxn: Int = 0,
    val driverLatLng: LatLng? = null,
    val driverHeadingDegrees: Float = 0f,
    val hasDriverGpsFix: Boolean = false,
    val feedLastUpdatedLabel: String = "En vivo",
    val autoCycleSecondsLeft: Int = 12,
    val autoCycleTotalSeconds: Int = 12,
    val isAutoRefreshPaused: Boolean = false,
    val pendingPassengerRatingTarget: DriverRideRequest? = null,
    val toastMessage: String? = null
) {
    fun visibleRequestsFor(role: UserRole): List<DriverRideRequest> {
        val roleFiltered = when (role) {
            UserRole.DRIVER -> requestQueue.filter { !it.isDeliveryOrCommerce }
            UserRole.DELIVERY -> requestQueue.filter { it.isDeliveryOrCommerce }
            UserRole.BOTH -> when (selectedFeedFilter) {
                DriverFeedFilter.ALL -> requestQueue
                DriverFeedFilter.PASSENGERS -> requestQueue.filter { !it.isDeliveryOrCommerce }
                DriverFeedFilter.DELIVERY_EATS -> requestQueue.filter { it.isDeliveryOrCommerce }
            }
            UserRole.PASSENGER -> requestQueue
        }
        return roleFiltered.ifEmpty { requestQueue }
    }

    fun activeRequestFor(role: UserRole): DriverRideRequest {
        val visible = visibleRequestsFor(role)
        if (selectedRequestId.isNotBlank()) {
            visible.firstOrNull { it.id == selectedRequestId }?.let { return it }
        }
        return visible.getOrElse(currentRequestIndex % visible.size) { visible.first() }
    }

    val activeRequest: DriverRideRequest
        get() = activeRequestFor(UserRole.BOTH)
}

private fun defaultRequests(): List<DriverRideRequest> = listOf(
    DriverRideRequest(
        id = "req_pass_1",
        origin = "Parque Principal Centro",
        destination = "TecNM Carrillo Puerto",
        fareMxn = 45,
        passengerName = "María G.",
        distanceKm = "1.8 km",
        startLat = 19.5775,
        startLng = -88.0453,
        endLat = 19.5850,
        endLng = -88.0410,
        serviceTypeLabel = "Viaje Estándar",
        requestCategory = DriverRequestCategory.PASSENGER_RIDE,
        passengerRatingLabel = "★ 4.9"
    ),
    DriverRideRequest(
        id = "req_eats_cafe",
        origin = "Cafetería El Kiosco Maya (Centro)",
        destination = "TecNM Felipe Carrillo Puerto",
        fareMxn = 30,
        passengerName = "Andrea V.",
        distanceKm = "1.7 km",
        startLat = 19.5776,
        startLng = -88.0452,
        endLat = 19.5850,
        endLng = -88.0410,
        serviceTypeLabel = "Cafetería · ViaKaab Eats",
        requestCategory = DriverRequestCategory.FOOD_ORDER,
        orderDetails = "2x Frappé Horchata Espresso, 1x Club Sándwich + Papas",
        passengerRatingLabel = "★ 5.0"
    ),
    DriverRideRequest(
        id = "req_pass_2",
        origin = "Terminal ADO",
        destination = "Hospital General",
        fareMxn = 40,
        passengerName = "Carlos M.",
        distanceKm = "1.4 km",
        startLat = 19.5790,
        startLng = -88.0440,
        endLat = 19.5720,
        endLng = -88.0490,
        serviceTypeLabel = "Viaje Estándar",
        requestCategory = DriverRequestCategory.PASSENGER_RIDE,
        passengerRatingLabel = "★ 4.8"
    ),
    DriverRideRequest(
        id = "req_eats_fonda",
        origin = "Fonda La Mestiza FCP (Calle 65)",
        destination = "Col. Emiliano Zapata",
        fareMxn = 28,
        passengerName = "José Luis K.",
        distanceKm = "1.3 km",
        startLat = 19.5768,
        startLng = -88.0442,
        endLat = 19.5830,
        endLng = -88.0380,
        serviceTypeLabel = "Fonda / Restaurante",
        requestCategory = DriverRequestCategory.FOOD_ORDER,
        orderDetails = "1x Comida Corrida Poc Chuc, 1x Agua de Chaya 1 L",
        passengerRatingLabel = "★ 4.9"
    ),
    DriverRideRequest(
        id = "req_eats_farmacia",
        origin = "Farmacia Yza Carrillo (Av. Benito Juárez)",
        destination = "Col. Jesús Martínez Ross",
        fareMxn = 25,
        passengerName = "Elena C.",
        distanceKm = "1.1 km",
        startLat = 19.5772,
        startLng = -88.0446,
        endLat = 19.5812,
        endLng = -88.0415,
        serviceTypeLabel = "Farmacia Express",
        requestCategory = DriverRequestCategory.PHARMACY_ORDER,
        orderDetails = "1x Paracetamol 500mg, 2x Suero Electrolit 625ml",
        passengerRatingLabel = "★ 5.0"
    ),
    DriverRideRequest(
        id = "req_eats_super",
        origin = "Chedraui Felipe Carrillo Puerto",
        destination = "Col. Cecilio Chi",
        fareMxn = 35,
        passengerName = "Familia Poot",
        distanceKm = "2.2 km",
        startLat = 19.5825,
        startLng = -88.0418,
        endLat = 19.5745,
        endLng = -88.0470,
        serviceTypeLabel = "Supermercado / Despensa",
        requestCategory = DriverRequestCategory.SUPERMARKET_ORDER,
        orderDetails = "1x Reja de Huevo 30 pzas, 1x Garrafón 20L, 2x Leche 1L",
        passengerRatingLabel = "★ 4.9"
    ),
    DriverRideRequest(
        id = "req_pass_3",
        origin = "Col. Cecilio Chi",
        destination = "Centro · Banco / Parque",
        fareMxn = 35,
        passengerName = "Don Roberto C.",
        distanceKm = "1.2 km",
        startLat = 19.5745,
        startLng = -88.0470,
        endLat = 19.5775,
        endLng = -88.0453,
        serviceTypeLabel = "Asistencia Adulto Mayor",
        requestCategory = DriverRequestCategory.PASSENGER_RIDE,
        passengerRatingLabel = "★ 5.0"
    ),
    DriverRideRequest(
        id = "req_parcel_1",
        origin = "Mercado Municipal Benito Juárez",
        destination = "Col. Emiliano Zapata",
        fareMxn = 35,
        passengerName = "Lucía P.",
        distanceKm = "1.1 km",
        startLat = 19.5760,
        startLng = -88.0430,
        endLat = 19.5830,
        endLng = -88.0380,
        serviceTypeLabel = "Envío de Paquete",
        requestCategory = DriverRequestCategory.PARCEL_DELIVERY,
        orderDetails = "Paquete mediano · Entregar a recepción",
        passengerRatingLabel = "★ 4.8"
    )
)

class MapViewModel(
    private val sessionRepository: SessionRepository,
    private val mapsRepository: GoogleMapsRepository
) : ViewModel() {

    val session = sessionRepository.session
    val supportPhone = sessionRepository.supportPhone
    val trustedContactPhone = sessionRepository.trustedContactPhone
    val quickLandmarks: List<CarrilloLandmark> = mapsRepository.quickLandmarks

    private val _passengerState = MutableStateFlow(PassengerMapUiState())
    val passengerState: StateFlow<PassengerMapUiState> = _passengerState.asStateFlow()

    private val _driverState = MutableStateFlow(DriverMapUiState())
    val driverState: StateFlow<DriverMapUiState> = _driverState.asStateFlow()

    private val _onlineUnits = MutableStateFlow<List<OnlineMapUnit>>(emptyList())
    val onlineUnits: StateFlow<List<OnlineMapUnit>> = _onlineUnits.asStateFlow()

    private var geocodeJob: Job? = null

    init {
        viewModelScope.launch {
            sessionRepository.session.collect {
                refreshOnlineUnits()
            }
        }
        viewModelScope.launch {
            sessionRepository.vehicleInfo.collect {
                refreshOnlineUnits()
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                tickDriverAutoFeed()
            }
        }
    }

    fun applyEntryContext(
        mode: ServiceMode,
        assistanceNote: String = "",
        initialDestinationQuery: String = ""
    ) {
        _passengerState.update { state ->
            state.copy(
                selectedMode = mode,
                assistanceNote = if (assistanceNote.isNotBlank() || state.activeLiveTrip == null) {
                    assistanceNote
                } else {
                    state.assistanceNote
                },
                suggestedFareMxn = state.estimatedDistanceKm?.let { mapsRepository.suggestFareMxn(it, mode) }
            )
        }
        if (initialDestinationQuery.isNotBlank()) {
            val match = quickLandmarks.firstOrNull {
                it.label.equals(initialDestinationQuery, ignoreCase = true) ||
                    it.query.contains(initialDestinationQuery, ignoreCase = true)
            }
            if (match != null) {
                selectQuickLandmark(match)
            } else {
                updateDestination(initialDestinationQuery)
            }
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
        _passengerState.update {
            it.copy(
                pickupText = value,
                isUsingRealTimeLocation = false
            )
        }
    }

    fun resetPickupToRealTimeGps() {
        val current = _passengerState.value.userLatLng
        _passengerState.update {
            it.copy(
                isUsingRealTimeLocation = true,
                pickupText = "Ubicación actual en tiempo real (GPS)",
                toastMessage = "Recogida vinculada a tu ubicación GPS en tiempo real"
            )
        }
        viewModelScope.launch {
            val address = mapsRepository.reverseGeocode(current.latitude, current.longitude)
            _passengerState.update { state ->
                if (state.isUsingRealTimeLocation) {
                    state.copy(pickupText = "En vivo: $address")
                } else {
                    state
                }
            }
        }
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
        val timeNow = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("es-MX")).format(Date())
        _passengerState.update { state ->
            val dist = state.destinationLatLng?.let { mapsRepository.calculateDistanceKm(latLng, it) }
            val updatedLive = state.activeLiveTrip?.let { trip ->
                val remainingKm = mapsRepository.calculateDistanceKm(
                    latLng,
                    LatLng(trip.destLat, trip.destLng)
                )
                val eta = (remainingKm * 2.5).roundToInt().coerceAtLeast(1)
                if (trip.phase == LiveTripPhase.IN_PROGRESS) {
                    trip.copy(driverLat = lat, driverLng = lng, etaMinutes = eta)
                } else {
                    trip.copy(pickupLat = lat, pickupLng = lng)
                }
            }
            state.copy(
                userLatLng = latLng,
                hasGpsFix = true,
                lastGpsUpdatedLabel = "GPS en vivo · $timeNow",
                estimatedDistanceKm = dist,
                suggestedFareMxn = dist?.let { mapsRepository.suggestFareMxn(it, state.selectedMode) },
                activeLiveTrip = updatedLive
            )
        }
        refreshOnlineUnits()
        if (_passengerState.value.isUsingRealTimeLocation) {
            viewModelScope.launch {
                val address = mapsRepository.reverseGeocode(lat, lng)
                _passengerState.update { state ->
                    if (state.isUsingRealTimeLocation) {
                        state.copy(pickupText = "En vivo: $address")
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun onDriverLocationResolved(lat: Double, lng: Double, gpsBearing: Float? = null) {
        val newLatLng = LatLng(lat, lng)
        _driverState.update { dState ->
            val prev = dState.driverLatLng
            val computedBearing = when {
                gpsBearing != null && gpsBearing >= 0f -> gpsBearing
                prev != null && mapsRepository.calculateDistanceKm(prev, newLatLng) > 0.002 ->
                    computeGeoBearing(prev, newLatLng)
                else -> dState.driverHeadingDegrees
            }
            dState.copy(
                driverLatLng = newLatLng,
                driverHeadingDegrees = computedBearing,
                hasDriverGpsFix = true,
                requestQueue = recomputeQueueDistances(dState.requestQueue, newLatLng)
            )
        }
        _passengerState.update { pState ->
            val trip = pState.activeLiveTrip
            if (trip != null && (trip.phase == LiveTripPhase.DRIVER_ARRIVING || trip.phase == LiveTripPhase.IN_PROGRESS)) {
                val remainingTarget = if (trip.phase == LiveTripPhase.DRIVER_ARRIVING) {
                    LatLng(trip.pickupLat, trip.pickupLng)
                } else {
                    LatLng(trip.destLat, trip.destLng)
                }
                val km = mapsRepository.calculateDistanceKm(newLatLng, remainingTarget)
                val eta = (km * 2.5).roundToInt().coerceAtLeast(1)
                pState.copy(
                    activeLiveTrip = trip.copy(
                        driverLat = lat,
                        driverLng = lng,
                        etaMinutes = eta
                    )
                )
            } else {
                pState
            }
        }
        refreshOnlineUnits()
    }

    private fun computeGeoBearing(from: LatLng, to: LatLng): Float {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
    }

    private fun refreshOnlineUnits() {
        val currentSession = sessionRepository.session.value
        val vehicle = sessionRepository.vehicleInfo.value
        val dState = _driverState.value
        val pState = _passengerState.value

        val units = mutableListOf<OnlineMapUnit>()

        // Only emit the local operator unit if their active role is Driver/Delivery/Both AND they are Online AND have a real GPS fix
        val activeDriverPos = dState.driverLatLng ?: if (pState.hasGpsFix) pState.userLatLng else null
        if (currentSession.role.isDriverRole && dState.isOnline && activeDriverPos != null) {
            val isMoto = currentSession.role == UserRole.DELIVERY ||
                vehicle.vehicleType.contains("moto", ignoreCase = true) ||
                vehicle.vehicleType.contains("bici", ignoreCase = true)
            units.add(
                OnlineMapUnit(
                    id = "self_online_unit",
                    label = currentSession.effectiveDisplayName,
                    vehicleType = if (isMoto) MapVehicleType.MOTO else MapVehicleType.CAR,
                    role = currentSession.role,
                    lat = activeDriverPos.latitude,
                    lng = activeDriverPos.longitude,
                    headingDegrees = dState.driverHeadingDegrees,
                    isPrimary = true
                )
            )
        }

        // Only emit assigned active trip unit when a real driver has accepted the trip
        val liveTrip = pState.activeLiveTrip
        if (liveTrip != null &&
            (liveTrip.phase == LiveTripPhase.DRIVER_ARRIVING || liveTrip.phase == LiveTripPhase.IN_PROGRESS) &&
            units.none { it.isPrimary }
        ) {
            val isDeliveryTrip = liveTrip.mode == ServiceMode.DELIVERY
            units.add(
                OnlineMapUnit(
                    id = "assigned_${liveTrip.id}",
                    label = liveTrip.driverName,
                    vehicleType = if (isDeliveryTrip) MapVehicleType.MOTO else MapVehicleType.CAR,
                    role = if (isDeliveryTrip) UserRole.DELIVERY else UserRole.DRIVER,
                    lat = liveTrip.driverLat,
                    lng = liveTrip.driverLng,
                    headingDegrees = dState.driverHeadingDegrees,
                    isPrimary = true
                )
            )
        }

        _onlineUnits.value = units
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
        val originName = state.pickupText.ifBlank { "Ubicación actual en tiempo real" }
        sessionRepository.addTripRequest(
            origin = originName,
            destination = state.destinationText,
            fareMxn = state.fareOfferText,
            mode = state.selectedMode
        )

        val destLatLng = state.destinationLatLng ?: LatLng(19.5850, -88.0410)
        val parsedFare = state.fareOfferText.toIntOrNull() ?: (state.suggestedFareMxn ?: 25)
        val passengerName = sessionRepository.session.value.effectiveDisplayName
        val distKmValue = state.estimatedDistanceKm
            ?: mapsRepository.calculateDistanceKm(state.userLatLng, destLatLng)
        val distLabel = "${"%.1f".format(distKmValue)} km"
        val tripId = "req_${System.currentTimeMillis()}"

        val reqCategory = if (state.selectedMode == ServiceMode.DELIVERY) {
            DriverRequestCategory.PARCEL_DELIVERY
        } else {
            DriverRequestCategory.PASSENGER_RIDE
        }
        val newDriverRequest = DriverRideRequest(
            id = tripId,
            origin = originName,
            destination = state.destinationText,
            fareMxn = parsedFare,
            passengerName = passengerName,
            distanceKm = distLabel,
            startLat = state.userLatLng.latitude,
            startLng = state.userLatLng.longitude,
            endLat = destLatLng.latitude,
            endLng = destLatLng.longitude,
            serviceTypeLabel = state.selectedMode.label,
            requestCategory = reqCategory,
            orderDetails = if (state.selectedMode == ServiceMode.DELIVERY) {
                state.packageDetailsText.ifBlank { "Envío solicitado desde mapa" }
            } else {
                state.assistanceNote
            },
            passengerRatingLabel = "★ ${sessionRepository.passengerRating.value.displayLabel}"
        )

        val liveTrip = ActiveLiveTrip(
            id = tripId,
            origin = originName,
            destination = state.destinationText,
            pickupLat = state.userLatLng.latitude,
            pickupLng = state.userLatLng.longitude,
            destLat = destLatLng.latitude,
            destLng = destLatLng.longitude,
            fareMxn = parsedFare,
            mode = state.selectedMode,
            driverName = "Buscando conductor",
            vehicleSummary = "Unidades en Felipe Carrillo Puerto",
            phase = LiveTripPhase.SEARCHING,
            driverLat = state.userLatLng.latitude,
            driverLng = state.userLatLng.longitude,
            etaMinutes = (distKmValue * 2.5).roundToInt().coerceAtLeast(2)
        )

        val timeNow = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("es-MX")).format(Date())
        _driverState.update { dState ->
            dState.copy(
                requestQueue = listOf(newDriverRequest) + dState.requestQueue,
                selectedRequestId = newDriverRequest.id,
                currentRequestIndex = 0,
                tripStage = DriverTripStage.OFFER_AVAILABLE,
                autoCycleSecondsLeft = dState.autoCycleTotalSeconds,
                feedLastUpdatedLabel = "Nueva solicitud · $timeNow"
            )
        }

        _passengerState.update {
            it.copy(
                activeLiveTrip = liveTrip,
                isLiveTripPanelExpanded = true,
                toastMessage = "¡Oferta publicada! Sigue tu viaje en tiempo real."
            )
        }
    }

    fun publishStoreDeliveryOrder(
        commerce: LocalCommerce,
        cartItems: List<CartItem>,
        deliveryAddress: String,
        orderNotes: String
    ) {
        if (cartItems.isEmpty()) return
        val itemsSubtotal = cartItems.sumOf { it.lineTotalMxn }
        val totalMxn = itemsSubtotal + commerce.deliveryFeeMxn
        val itemsSummary = cartItems.joinToString(", ") { "${it.quantity}x ${it.product.name}" }
        val cleanDest = deliveryAddress.trim().ifBlank { "Centro, Felipe Carrillo Puerto" }
        val storeOrigin = "${commerce.name} (${commerce.neighborhood})"
        val noteSuffix = orderNotes.trim().takeIf { it.isNotEmpty() }?.let { " · Nota: $it" }.orEmpty()

        sessionRepository.addTripRequest(
            origin = storeOrigin,
            destination = "$cleanDest · $itemsSummary",
            fareMxn = "$totalMxn",
            mode = ServiceMode.DELIVERY
        )

        val currentDestLatLng = _passengerState.value.userLatLng
        val storeLatLng = LatLng(commerce.lat, commerce.lng)
        val distKmValue = mapsRepository.calculateDistanceKm(storeLatLng, currentDestLatLng)
            .coerceAtLeast(0.6)
        val distLabel = "${"%.1f".format(distKmValue)} km"
        val tripId = "eats_${System.currentTimeMillis()}"
        val passengerName = sessionRepository.session.value.effectiveDisplayName

        val reqCat = when (commerce.category) {
            EatsCategory.FARMACIA -> DriverRequestCategory.PHARMACY_ORDER
            EatsCategory.SUPER -> DriverRequestCategory.SUPERMARKET_ORDER
            else -> DriverRequestCategory.FOOD_ORDER
        }
        val driverRequest = DriverRideRequest(
            id = tripId,
            origin = storeOrigin,
            destination = cleanDest,
            fareMxn = commerce.deliveryFeeMxn,
            passengerName = passengerName,
            distanceKm = distLabel,
            startLat = commerce.lat,
            startLng = commerce.lng,
            endLat = currentDestLatLng.latitude,
            endLng = currentDestLatLng.longitude,
            serviceTypeLabel = "${commerce.category.label} · ViaKaab Eats",
            requestCategory = reqCat,
            orderDetails = "$itemsSummary$noteSuffix",
            passengerRatingLabel = "★ ${sessionRepository.passengerRating.value.displayLabel}"
        )

        val liveTrip = ActiveLiveTrip(
            id = tripId,
            origin = storeOrigin,
            destination = cleanDest,
            pickupLat = commerce.lat,
            pickupLng = commerce.lng,
            destLat = currentDestLatLng.latitude,
            destLng = currentDestLatLng.longitude,
            fareMxn = totalMxn,
            mode = ServiceMode.DELIVERY,
            driverName = "Buscando repartidor",
            vehicleSummary = "Moto / Unidad ViaKaab Eats · $itemsSummary",
            phase = LiveTripPhase.SEARCHING,
            driverLat = commerce.lat,
            driverLng = commerce.lng,
            etaMinutes = (distKmValue * 3.0).roundToInt().coerceIn(8, 30)
        )

        val timeNow = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("es-MX")).format(Date())
        _driverState.update { dState ->
            dState.copy(
                requestQueue = listOf(driverRequest) + dState.requestQueue,
                selectedRequestId = driverRequest.id,
                currentRequestIndex = 0,
                tripStage = DriverTripStage.OFFER_AVAILABLE,
                autoCycleSecondsLeft = dState.autoCycleTotalSeconds,
                feedLastUpdatedLabel = "Nuevo pedido · $timeNow"
            )
        }

        _passengerState.update {
            it.copy(
                selectedMode = ServiceMode.DELIVERY,
                isUsingRealTimeLocation = false,
                pickupText = storeOrigin,
                destinationText = cleanDest,
                destinationLatLng = currentDestLatLng,
                destinationMarkerTitle = "Entrega: $cleanDest",
                packageDetailsText = "$itemsSummary$noteSuffix",
                fareOfferText = totalMxn.toString(),
                assistanceNote = "Pedido ${commerce.name}: $itemsSummary",
                activeLiveTrip = liveTrip,
                isLiveTripPanelExpanded = true,
                toastMessage = "¡Pedido enviado a ${commerce.name}! Total $$totalMxn MXN"
            )
        }
    }

    fun setLiveTripPanelExpanded(expanded: Boolean) {
        _passengerState.update { it.copy(isLiveTripPanelExpanded = expanded) }
    }

    fun closeLiveTrip() {
        _passengerState.update {
            it.copy(
                activeLiveTrip = null,
                isLiveTripPanelExpanded = false,
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
                toastMessage = "Seguimiento finalizado. Listo para un nuevo viaje."
            )
        }
        refreshOnlineUnits()
    }

    fun saveSupportPhone(phone: String) {
        sessionRepository.saveSupportPhone(phone)
        _passengerState.update {
            it.copy(toastMessage = "Teléfono de soporte ViaKaab guardado: ${phone.trim()}")
        }
    }

    fun saveTrustedContactPhone(phone: String) {
        sessionRepository.saveTrustedContactPhone(phone)
        _passengerState.update {
            it.copy(toastMessage = "Contacto de confianza guardado")
        }
    }

    fun buildWhatsAppShareMessage(): String {
        val state = _passengerState.value
        val userName = sessionRepository.session.value.effectiveDisplayName
        val lat = state.userLatLng.latitude
        val lng = state.userLatLng.longitude
        val mapsLink = "https://maps.google.com/?q=${"%.6f".format(lat)},${"%.6f".format(lng)}"
        val live = state.activeLiveTrip
        return buildString {
            append("🛡️ *ViaKaab · Ubicación y Recorrido en Tiempo Real*\n")
            append("Pasajero: $userName\n")
            if (live != null) {
                append("Estado: ${live.phase.label}\n")
                append("Origen: ${live.origin}\n")
                append("Destino: ${live.destination}\n")
                append("Tarifa: $${live.fareMxn} MXN (${live.mode.label})\n")
            } else {
                append("Origen: ${state.pickupText}\n")
                if (state.destinationText.isNotBlank()) {
                    append("Destino: ${state.destinationText}\n")
                }
                if (state.fareOfferText.isNotBlank()) {
                    append("Tarifa ofertada: $${state.fareOfferText} MXN\n")
                }
            }
            append("📍 Ubicación actual en vivo:\n$mapsLink")
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
        refreshOnlineUnits()
    }

    fun setDriverAutoRefreshPaused(paused: Boolean) {
        _driverState.update { it.copy(isAutoRefreshPaused = paused) }
    }

    fun refreshDriverFeedNow() {
        val role = sessionRepository.session.value.role
        val timeNow = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("es-MX")).format(Date())
        _driverState.update { state ->
            val visible = state.visibleRequestsFor(role)
            val currentIdx = visible.indexOfFirst { it.id == state.activeRequestFor(role).id }
                .coerceAtLeast(0)
            val nextIdx = (currentIdx + 1) % visible.size
            val nextReq = visible[nextIdx]
            state.copy(
                requestQueue = recomputeQueueDistances(state.requestQueue, state.driverLatLng),
                selectedRequestId = nextReq.id,
                currentRequestIndex = nextIdx,
                autoCycleSecondsLeft = state.autoCycleTotalSeconds,
                feedLastUpdatedLabel = "Actualizado · $timeNow"
            )
        }
    }

    private fun tickDriverAutoFeed() {
        val role = sessionRepository.session.value.role
        val current = _driverState.value
        if (!role.isDriverRole ||
            !current.isOnline ||
            current.tripStage != DriverTripStage.OFFER_AVAILABLE ||
            current.isAutoRefreshPaused
        ) {
            return
        }
        val nextSec = current.autoCycleSecondsLeft - 1
        if (nextSec > 0) {
            _driverState.update { it.copy(autoCycleSecondsLeft = nextSec) }
        } else {
            refreshDriverFeedNow()
        }
    }

    private fun recomputeQueueDistances(
        queue: List<DriverRideRequest>,
        driverPos: LatLng?
    ): List<DriverRideRequest> {
        return queue.map { req ->
            val tripKm = mapsRepository.calculateDistanceKm(
                LatLng(req.startLat, req.startLng),
                LatLng(req.endLat, req.endLng)
            )
            val label = if (driverPos != null) {
                val pickupKm = mapsRepository.calculateDistanceKm(
                    driverPos,
                    LatLng(req.startLat, req.startLng)
                )
                if (pickupKm <= 50.0) {
                    "${"%.1f".format(pickupKm)} km de ti · ${"%.1f".format(tripKm)} km"
                } else {
                    "${"%.1f".format(tripKm)} km"
                }
            } else {
                "${"%.1f".format(tripKm)} km"
            }
            req.copy(distanceKm = label)
        }
    }

    fun setDriverFeedFilter(filter: DriverFeedFilter) {
        _driverState.update {
            it.copy(
                selectedFeedFilter = filter,
                selectedRequestId = "",
                currentRequestIndex = 0,
                autoCycleSecondsLeft = it.autoCycleTotalSeconds
            )
        }
    }

    fun selectDriverRequest(requestId: String) {
        _driverState.update {
            it.copy(
                selectedRequestId = requestId,
                tripStage = DriverTripStage.OFFER_AVAILABLE,
                autoCycleSecondsLeft = it.autoCycleTotalSeconds
            )
        }
    }

    fun sendCounterOffer(amount: String) {
        val parsed = amount.toIntOrNull() ?: return
        val role = sessionRepository.session.value.role
        _driverState.update { state ->
            val targetId = state.activeRequestFor(role).id
            val updatedQueue = state.requestQueue.map { req ->
                if (req.id == targetId) req.copy(fareMxn = parsed) else req
            }
            state.copy(
                requestQueue = updatedQueue,
                toastMessage = "Contraoferta de $$parsed MXN enviada al cliente"
            )
        }
    }

    fun skipDriverRequest() {
        val role = sessionRepository.session.value.role
        _driverState.update { state ->
            val visible = state.visibleRequestsFor(role)
            val currentIdx = visible.indexOfFirst { it.id == state.activeRequestFor(role).id }
                .coerceAtLeast(0)
            val nextReq = visible[(currentIdx + 1) % visible.size]
            state.copy(
                selectedRequestId = nextReq.id,
                currentRequestIndex = (currentIdx + 1) % visible.size,
                tripStage = DriverTripStage.OFFER_AVAILABLE,
                toastMessage = "Mostrando siguiente solicitud disponible"
            )
        }
    }

    fun startDriverRoute() {
        val role = sessionRepository.session.value.role
        val req = _driverState.value.activeRequestFor(role)
        val driverDisplay = sessionRepository.session.value.effectiveDisplayName
        val vehicle = sessionRepository.vehicleInfo.value
        val vehicleLabel = if (vehicle.isRegistered) {
            "${vehicle.brand} ${vehicle.model} · ${vehicle.plate}"
        } else {
            "Unidad ViaKaab verificada"
        }
        _driverState.update {
            it.copy(
                selectedRequestId = req.id,
                tripStage = DriverTripStage.HEADING_TO_PICKUP,
                toastMessage = if (req.isDeliveryOrCommerce) {
                    "En camino a recoger pedido en ${req.origin}"
                } else {
                    "Ruta iniciada hacia el pasajero"
                }
            )
        }
        _passengerState.update { pState ->
            val currentLive = pState.activeLiveTrip
            if (currentLive != null && currentLive.id == req.id) {
                pState.copy(
                    activeLiveTrip = currentLive.copy(
                        phase = LiveTripPhase.DRIVER_ARRIVING,
                        driverName = driverDisplay,
                        vehicleSummary = vehicleLabel,
                        driverLat = _driverState.value.driverLatLng?.latitude ?: req.startLat,
                        driverLng = _driverState.value.driverLatLng?.longitude ?: req.startLng,
                        etaMinutes = 3
                    )
                )
            } else {
                pState
            }
        }
        refreshOnlineUnits()
    }

    fun advanceDriverTripStage() {
        val role = sessionRepository.session.value.role
        val currentReq = _driverState.value.activeRequestFor(role)
        _driverState.update { state ->
            when (state.tripStage) {
                DriverTripStage.OFFER_AVAILABLE -> state.copy(
                    tripStage = DriverTripStage.HEADING_TO_PICKUP,
                    toastMessage = "En camino al punto de recogida"
                )
                DriverTripStage.HEADING_TO_PICKUP -> {
                    _passengerState.update { pState ->
                        val live = pState.activeLiveTrip
                        if (live != null && live.id == currentReq.id) {
                            pState.copy(activeLiveTrip = live.copy(phase = LiveTripPhase.IN_PROGRESS))
                        } else {
                            pState
                        }
                    }
                    state.copy(
                        tripStage = DriverTripStage.TRIP_IN_PROGRESS,
                        toastMessage = if (currentReq.isDeliveryOrCommerce) {
                            "Pedido recogido · En camino a entregar"
                        } else {
                            "¡Pasajero a bordo! Viaje en curso"
                        }
                    )
                }
                DriverTripStage.TRIP_IN_PROGRESS -> {
                    val fare = currentReq.fareMxn
                    sessionRepository.addTripRequest(
                        origin = currentReq.origin,
                        destination = currentReq.destination,
                        fareMxn = "$fare",
                        mode = if (currentReq.isDeliveryOrCommerce) ServiceMode.DELIVERY else ServiceMode.STANDARD
                    )
                    _passengerState.update { pState ->
                        val live = pState.activeLiveTrip
                        if (live != null && live.id == currentReq.id) {
                            pState.copy(
                                activeLiveTrip = live.copy(
                                    phase = LiveTripPhase.COMPLETED,
                                    etaMinutes = 0
                                )
                            )
                        } else {
                            pState
                        }
                    }
                    val visible = state.visibleRequestsFor(role)
                    val curIdx = visible.indexOfFirst { it.id == currentReq.id }.coerceAtLeast(0)
                    val nextReq = visible[(curIdx + 1) % visible.size]
                    state.copy(
                        tripStage = DriverTripStage.OFFER_AVAILABLE,
                        selectedRequestId = nextReq.id,
                        currentRequestIndex = (curIdx + 1) % visible.size,
                        completedTripsToday = state.completedTripsToday + 1,
                        earningsTodayMxn = state.earningsTodayMxn + fare,
                        pendingPassengerRatingTarget = currentReq,
                        toastMessage = "¡Servicio finalizado! +$$fare MXN registrados"
                    )
                }
            }
        }
        refreshOnlineUnits()
    }

    fun submitPassengerRating(stars: Int, comment: String) {
        val role = sessionRepository.session.value.role
        val raterLabel = when (role) {
            com.example.viakaab.model.UserRole.DELIVERY -> "Repartidor"
            com.example.viakaab.model.UserRole.BOTH -> "Conductor / Repartidor"
            else -> "Conductor"
        }
        sessionRepository.addPassengerRating(stars, raterLabel, comment)
        _driverState.update {
            it.copy(
                pendingPassengerRatingTarget = null,
                toastMessage = "Calificación de ★ $stars enviada al pasajero"
            )
        }
    }

    fun dismissPassengerRating() {
        _driverState.update { it.copy(pendingPassengerRatingTarget = null) }
    }

    fun clearDriverToast() {
        _driverState.update { it.copy(toastMessage = null) }
    }
}
