package com.example.viakaab.model

import kotlinx.serialization.Serializable

enum class UserRole(val key: String, val displayName: String) {
    PASSENGER("passenger", "Pasajero"),
    DRIVER("driver", "Conductor"),
    DELIVERY("delivery", "Repartidor"),
    BOTH("both", "Conductor y Repartidor");

    val isDriverRole: Boolean
        get() = this == DRIVER || this == DELIVERY || this == BOTH

    companion object {
        fun fromKey(key: String?): UserRole =
            entries.firstOrNull { it.key == key } ?: PASSENGER
    }
}

@Serializable
enum class ServiceMode(val label: String) {
    STANDARD("Estándar"),
    INTERCITY("Ciudad a Ciudad"),
    DELIVERY("Entregas")
}

enum class PaymentMethodType(val title: String) {
    CASH("Efectivo (Pago directo)"),
    CARD("Tarjeta de Crédito / Débito"),
    SPEI("Transferencia Bancaria SPEI")
}

enum class DriverTripStage {
    OFFER_AVAILABLE,
    HEADING_TO_PICKUP,
    TRIP_IN_PROGRESS
}

enum class DriverRequestCategory(val label: String) {
    PASSENGER_RIDE("Viaje Pasajero"),
    FOOD_ORDER("Comida / Cafetería / Fonda"),
    PHARMACY_ORDER("Farmacia"),
    SUPERMARKET_ORDER("Súper / Despensa"),
    PARCEL_DELIVERY("Envío de Paquete")
}

enum class DriverFeedFilter(val label: String) {
    ALL("Todos"),
    PASSENGERS("Pasajeros"),
    DELIVERY_EATS("Comida y Envíos")
}

enum class LiveTripPhase(val label: String) {
    SEARCHING("Buscando conductor cercano..."),
    DRIVER_ARRIVING("Conductor en camino hacia ti"),
    IN_PROGRESS("Viaje en curso en tiempo real"),
    COMPLETED("Viaje finalizado")
}

data class ActiveLiveTrip(
    val id: String,
    val origin: String,
    val destination: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val destLat: Double,
    val destLng: Double,
    val fareMxn: Int,
    val mode: ServiceMode,
    val driverName: String = "Por asignar",
    val vehicleSummary: String = "Unidad ViaKaab",
    val phase: LiveTripPhase = LiveTripPhase.SEARCHING,
    val driverLat: Double = pickupLat,
    val driverLng: Double = pickupLng,
    val etaMinutes: Int = 4
)

enum class MapVehicleType {
    CAR,
    MOTO
}

data class OnlineMapUnit(
    val id: String,
    val label: String,
    val vehicleType: MapVehicleType,
    val role: UserRole,
    val lat: Double,
    val lng: Double,
    val headingDegrees: Float = 0f,
    val isPrimary: Boolean = false
)

data class UserSession(
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val profileImageUri: String = "",
    val role: UserRole = UserRole.PASSENGER,
    val accessToken: String = "",
    val savedEmail: String = "",
    val savedPassword: String = ""
) {
    val effectiveDisplayName: String
        get() {
            if (name.isNotBlank()) return name
            if (email.contains("@")) {
                val prefix = email.substringBefore("@")
                return prefix.replaceFirstChar { it.uppercase() }
            }
            return "Usuario ViaKaab"
        }

    val initials: String
        get() {
            val display = effectiveDisplayName.trim()
            return if (display.length >= 2) display.take(2).uppercase() else "VK"
        }
}

@Serializable
data class TripRecord(
    val id: String,
    val origin: String,
    val destination: String,
    val fareMxn: String,
    val mode: ServiceMode,
    val timestampLabel: String,
    val statusLabel: String
)

@Serializable
data class ScheduledRide(
    val id: String,
    val destination: String,
    val dateLabel: String,
    val timeLabel: String,
    val mode: ServiceMode,
    val notes: String = ""
)

@Serializable
data class VehicleInfo(
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val plate: String = "",
    val vehicleType: String = "",
    val syncedWithServer: Boolean = false
) {
    val isRegistered: Boolean
        get() = brand.isNotBlank() && model.isNotBlank() && plate.isNotBlank()
}

@Serializable
data class SavedCardInfo(
    val alias: String = "",
    val last4: String = "",
    val brandLabel: String = "Tarjeta"
)

data class InboxMessage(
    val id: String,
    val title: String,
    val body: String,
    val timeLabel: String,
    val isUnread: Boolean = true
)

data class DriverRideRequest(
    val id: String,
    val origin: String,
    val destination: String,
    val fareMxn: Int,
    val passengerName: String,
    val distanceKm: String,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val serviceTypeLabel: String = "Viaje Estándar",
    val requestCategory: DriverRequestCategory = DriverRequestCategory.PASSENGER_RIDE,
    val orderDetails: String = "",
    val passengerRatingLabel: String = "★ 4.9"
) {
    val isDeliveryOrCommerce: Boolean
        get() = requestCategory != DriverRequestCategory.PASSENGER_RIDE
}

@Serializable
data class DocumentVerificationState(
    val ineStatus: String = "Pendiente de revisión",
    val ineUploaded: Boolean = false,
    val licenseStatus: String = "Pendiente de revisión",
    val licenseUploaded: Boolean = false,
    val insuranceStatus: String = "No adjuntado",
    val insuranceUploaded: Boolean = false,
    val faceVerified: Boolean = false,
    val ineScanned: Boolean = false
) {
    val completedStepsCount: Int
        get() = listOf(ineUploaded, licenseUploaded, faceVerified, ineScanned).count { it }
}

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isError: Boolean = false
)

@Serializable
data class PassengerRatingState(
    val totalStars: Int = 0,
    val ratingCount: Int = 0,
    val lastRaterRole: String = "",
    val lastComment: String = ""
) {
    val average: Double
        get() = if (ratingCount <= 0) 0.0 else totalStars.toDouble() / ratingCount

    val displayLabel: String
        get() = if (ratingCount <= 0) {
            "0"
        } else {
            String.format(java.util.Locale.US, "%.1f", average)
        }
}
