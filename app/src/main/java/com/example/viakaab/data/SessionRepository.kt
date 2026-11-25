package com.example.viakaab.data

import android.content.Context
import android.content.SharedPreferences
import com.example.viakaab.model.DocumentVerificationState
import com.example.viakaab.model.InboxMessage
import com.example.viakaab.model.PaymentMethodType
import com.example.viakaab.model.SavedCardInfo
import com.example.viakaab.model.ScheduledRide
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.model.TripRecord
import com.example.viakaab.model.UserRole
import com.example.viakaab.model.UserSession
import com.example.viakaab.model.VehicleInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    private val _session = MutableStateFlow(readSession())
    val session: StateFlow<UserSession> = _session.asStateFlow()

    private val _trips = MutableStateFlow(readTrips())
    val trips: StateFlow<List<TripRecord>> = _trips.asStateFlow()

    private val _scheduledRides = MutableStateFlow(readScheduledRides())
    val scheduledRides: StateFlow<List<ScheduledRide>> = _scheduledRides.asStateFlow()

    private val _selectedPayment = MutableStateFlow(readSelectedPayment())
    val selectedPayment: StateFlow<PaymentMethodType> = _selectedPayment.asStateFlow()

    private val _savedCard = MutableStateFlow(readSavedCard())
    val savedCard: StateFlow<SavedCardInfo?> = _savedCard.asStateFlow()

    private val _vehicleInfo = MutableStateFlow(readVehicleInfo())
    val vehicleInfo: StateFlow<VehicleInfo> = _vehicleInfo.asStateFlow()

    private val _docStatus = MutableStateFlow(readDocStatus())
    val docStatus: StateFlow<DocumentVerificationState> = _docStatus.asStateFlow()

    private val _inboxMessages = MutableStateFlow(
        listOf(
            InboxMessage(
                id = "msg_welcome",
                title = "¡Bienvenido a ViaKaab!",
                body = "Ya puedes solicitar viajes, envíos y viajes interurbanos en Felipe Carrillo Puerto negociando tu tarifa directa en $ MXN.",
                timeLabel = "Hoy",
                isUnread = true
            ),
            InboxMessage(
                id = "msg_promo",
                title = "Tarifa justa sin comisión oculta",
                body = "Propón tu tarifa ideal y recibe ofertas de conductores verificados cerca de ti.",
                timeLabel = "Promoción",
                isUnread = true
            )
        )
    )
    val inboxMessages: StateFlow<List<InboxMessage>> = _inboxMessages.asStateFlow()

    private fun readSession(): UserSession {
        return UserSession(
            isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false),
            email = prefs.getString(KEY_USER_EMAIL, "").orEmpty(),
            name = prefs.getString(KEY_USER_NAME, "").orEmpty(),
            phone = prefs.getString(KEY_USER_PHONE, "").orEmpty(),
            profileImageUri = prefs.getString(KEY_PROFILE_IMAGE_URI, "").orEmpty(),
            role = UserRole.fromKey(prefs.getString(KEY_USER_ROLE, UserRole.PASSENGER.key)),
            accessToken = prefs.getString(KEY_ACCESS_TOKEN, "").orEmpty(),
            savedEmail = prefs.getString(KEY_SAVED_EMAIL, "").orEmpty(),
            savedPassword = prefs.getString(KEY_SAVED_PASSWORD, "").orEmpty()
        )
    }

    private fun readTrips(): List<TripRecord> {
        val raw = prefs.getString(KEY_TRIPS_JSON, null)
        if (!raw.isNullOrBlank()) {
            runCatching {
                return json.decodeFromString<List<TripRecord>>(raw)
            }
        }
        return emptyList()
    }

    private fun persistTrips(list: List<TripRecord>) {
        prefs.edit().putString(KEY_TRIPS_JSON, json.encodeToString(list)).apply()
    }

    private fun readScheduledRides(): List<ScheduledRide> {
        val raw = prefs.getString(KEY_SCHEDULED_JSON, null)
        if (!raw.isNullOrBlank()) {
            runCatching {
                return json.decodeFromString<List<ScheduledRide>>(raw)
            }
        }
        return emptyList()
    }

    private fun persistScheduledRides(list: List<ScheduledRide>) {
        prefs.edit().putString(KEY_SCHEDULED_JSON, json.encodeToString(list)).apply()
    }

    private fun readSelectedPayment(): PaymentMethodType {
        val name = prefs.getString(KEY_SELECTED_PAYMENT, PaymentMethodType.CASH.name)
        return PaymentMethodType.entries.firstOrNull { it.name == name } ?: PaymentMethodType.CASH
    }

    private fun readSavedCard(): SavedCardInfo? {
        val raw = prefs.getString(KEY_SAVED_CARD_JSON, null) ?: return null
        return runCatching { json.decodeFromString<SavedCardInfo>(raw) }.getOrNull()
    }

    private fun readVehicleInfo(): VehicleInfo {
        val raw = prefs.getString(KEY_VEHICLE_JSON, null) ?: return VehicleInfo()
        return runCatching { json.decodeFromString<VehicleInfo>(raw) }.getOrDefault(VehicleInfo())
    }

    private fun readDocStatus(): DocumentVerificationState {
        val raw = prefs.getString(KEY_DOC_STATUS_JSON, null) ?: return DocumentVerificationState()
        return runCatching { json.decodeFromString<DocumentVerificationState>(raw) }
            .getOrDefault(DocumentVerificationState())
    }

    private fun persistDocStatus(state: DocumentVerificationState) {
        prefs.edit().putString(KEY_DOC_STATUS_JSON, json.encodeToString(state)).apply()
    }

    fun createLoginSession(email: String, name: String, role: UserRole, token: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_ROLE, role.key)
            .putString(KEY_ACCESS_TOKEN, token)
            .putBoolean(KEY_HAS_SEEN_WELCOME, true)
            .apply()
        _session.value = readSession()
    }

    fun saveCredentials(email: String, password: String) {
        prefs.edit()
            .putString(KEY_SAVED_EMAIL, email)
            .putString(KEY_SAVED_PASSWORD, password)
            .apply()
        _session.value = readSession()
    }

    fun updateProfile(name: String, phone: String, imageUri: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_PROFILE_IMAGE_URI, imageUri)
            .apply()
        _session.value = readSession()
    }

    fun setUserPhone(phone: String) {
        prefs.edit().putString(KEY_USER_PHONE, phone).apply()
        _session.value = readSession()
    }

    fun selectRole(role: UserRole) {
        val current = _session.value
        prefs.edit()
            .putString(KEY_SELECTED_ROLE, role.key)
            .putString(KEY_USER_ROLE, role.key)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_HAS_SEEN_WELCOME, true)
            .apply()
        _session.value = current.copy(role = role, isLoggedIn = true)
    }

    fun logout() {
        val savedEmail = prefs.getString(KEY_SAVED_EMAIL, "").orEmpty()
        val savedPassword = prefs.getString(KEY_SAVED_PASSWORD, "").orEmpty()
        prefs.edit()
            .clear()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_HAS_SEEN_WELCOME, true)
            .putString(KEY_SAVED_EMAIL, savedEmail)
            .putString(KEY_SAVED_PASSWORD, savedPassword)
            .apply()
        _session.value = readSession()
        _trips.value = emptyList()
        _scheduledRides.value = emptyList()
        _savedCard.value = null
        _vehicleInfo.value = VehicleInfo()
        _docStatus.value = DocumentVerificationState()
        _selectedPayment.value = PaymentMethodType.CASH
    }

    fun addTripRequest(origin: String, destination: String, fareMxn: String, mode: ServiceMode) {
        val statusText = when (mode) {
            ServiceMode.STANDARD -> "Viaje Estándar solicitado ✓"
            ServiceMode.INTERCITY -> "Viaje Interurbano solicitado ✓"
            ServiceMode.DELIVERY -> "Envío de paquete solicitado ✓"
        }
        val cleanFare = fareMxn.trim()
        val formattedFare = if (cleanFare.contains("MXN")) cleanFare else "$$cleanFare MXN"
        val timeStamp = SimpleDateFormat("dd MMM, h:mm a", Locale.forLanguageTag("es-MX")).format(Date())
        val newTrip = TripRecord(
            id = "trip_${System.currentTimeMillis()}",
            origin = origin,
            destination = destination,
            fareMxn = formattedFare,
            mode = mode,
            timestampLabel = timeStamp,
            statusLabel = statusText
        )
        _trips.update { current ->
            val updated = listOf(newTrip) + current
            persistTrips(updated)
            updated
        }
        _inboxMessages.update { msgs ->
            listOf(
                InboxMessage(
                    id = "inbox_trip_${System.currentTimeMillis()}",
                    title = "Solicitud publicada ($formattedFare)",
                    body = "$origin ➔ $destination · Buscando conductores cercanos.",
                    timeLabel = "Ahora",
                    isUnread = true
                )
            ) + msgs
        }
    }

    fun addScheduledRide(destination: String, dateLabel: String, timeLabel: String, mode: ServiceMode, notes: String) {
        val ride = ScheduledRide(
            id = "sched_${System.currentTimeMillis()}",
            destination = destination,
            dateLabel = dateLabel,
            timeLabel = timeLabel,
            mode = mode,
            notes = notes
        )
        _scheduledRides.update { current ->
            val updated = listOf(ride) + current
            persistScheduledRides(updated)
            updated
        }
    }

    fun removeScheduledRide(id: String) {
        _scheduledRides.update { current ->
            val updated = current.filterNot { it.id == id }
            persistScheduledRides(updated)
            updated
        }
    }

    fun selectPaymentMethod(method: PaymentMethodType) {
        prefs.edit().putString(KEY_SELECTED_PAYMENT, method.name).apply()
        _selectedPayment.value = method
    }

    fun saveMaskedCard(alias: String, last4: String, brandLabel: String) {
        val card = SavedCardInfo(alias = alias, last4 = last4, brandLabel = brandLabel)
        prefs.edit().putString(KEY_SAVED_CARD_JSON, json.encodeToString(card)).apply()
        _savedCard.value = card
        selectPaymentMethod(PaymentMethodType.CARD)
    }

    fun saveVehicleInfo(info: VehicleInfo) {
        prefs.edit().putString(KEY_VEHICLE_JSON, json.encodeToString(info)).apply()
        _vehicleInfo.value = info
    }

    fun markDocumentUploaded(type: String) {
        _docStatus.update { current ->
            val updated = when (type) {
                "ine" -> current.copy(
                    ineStatus = "Estado INE: Subida y en revisión ✓",
                    ineUploaded = true
                )
                "license" -> current.copy(
                    licenseStatus = "Estado Licencia: Subida y en revisión ✓",
                    licenseUploaded = true
                )
                "insurance" -> current.copy(
                    insuranceStatus = "Estado Seguro: Póliza adjuntada ✓",
                    insuranceUploaded = true
                )
                else -> current
            }
            persistDocStatus(updated)
            updated
        }
    }

    fun markFaceVerified() {
        _docStatus.update { current ->
            val updated = current.copy(faceVerified = true)
            persistDocStatus(updated)
            updated
        }
    }

    fun markIneScanned() {
        _docStatus.update { current ->
            val updated = current.copy(
                ineScanned = true,
                ineStatus = "Estado INE: Escaneada y verificada ✓",
                ineUploaded = true
            )
            persistDocStatus(updated)
            updated
        }
    }

    fun markAllInboxRead() {
        _inboxMessages.update { msgs -> msgs.map { it.copy(isUnread = false) } }
    }

    companion object {
        private const val PREF_NAME = "ViaKaabSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_PHONE = "userPhone"
        private const val KEY_PROFILE_IMAGE_URI = "profileImageUri"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_ACCESS_TOKEN = "accessToken"
        private const val KEY_SELECTED_ROLE = "selectedRole"
        private const val KEY_HAS_SEEN_WELCOME = "hasSeenWelcome"
        private const val KEY_SAVED_EMAIL = "savedEmail"
        private const val KEY_SAVED_PASSWORD = "savedPassword"
        private const val KEY_TRIPS_JSON = "tripsJson"
        private const val KEY_SCHEDULED_JSON = "scheduledJson"
        private const val KEY_SELECTED_PAYMENT = "selectedPayment"
        private const val KEY_SAVED_CARD_JSON = "savedCardJson"
        private const val KEY_VEHICLE_JSON = "vehicleJson"
        private const val KEY_DOC_STATUS_JSON = "docStatusJson"
    }
}
