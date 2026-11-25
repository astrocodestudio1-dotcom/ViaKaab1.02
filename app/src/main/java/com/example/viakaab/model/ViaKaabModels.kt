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
    val serviceTypeLabel: String = "Viaje Estándar"
)

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
