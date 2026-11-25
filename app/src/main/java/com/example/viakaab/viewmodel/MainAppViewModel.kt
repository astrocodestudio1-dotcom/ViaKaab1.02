package com.example.viakaab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viakaab.data.SessionRepository
import com.example.viakaab.data.SupabaseRepository
import com.example.viakaab.model.PaymentMethodType
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.model.UserRole
import com.example.viakaab.model.VehicleInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileActionState(
    val isSaving: Boolean = false,
    val message: String? = null
)

class MainAppViewModel(
    private val sessionRepository: SessionRepository,
    private val supabaseRepository: SupabaseRepository
) : ViewModel() {

    val session = sessionRepository.session
    val trips = sessionRepository.trips
    val scheduledRides = sessionRepository.scheduledRides
    val selectedPayment = sessionRepository.selectedPayment
    val savedCard = sessionRepository.savedCard
    val vehicleInfo = sessionRepository.vehicleInfo
    val docStatus = sessionRepository.docStatus
    val inboxMessages = sessionRepository.inboxMessages

    private val _actionState = MutableStateFlow(ProfileActionState())
    val actionState: StateFlow<ProfileActionState> = _actionState.asStateFlow()

    fun updateProfile(name: String, phone: String, imageUri: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _actionState.update { it.copy(isSaving = true, message = null) }
            val email = sessionRepository.session.value.email
            sessionRepository.updateProfile(name, phone, imageUri)
            val result = supabaseRepository.updateProfile(email, name, phone)
            result.onSuccess {
                _actionState.update { it.copy(isSaving = false, message = "¡Perfil actualizado correctamente!") }
                onSuccess()
            }.onFailure { err ->
                _actionState.update {
                    it.copy(
                        isSaving = false,
                        message = "Perfil guardado localmente. Sincronización Supabase: ${err.localizedMessage ?: "pendiente"}"
                    )
                }
                onSuccess()
            }
        }
    }

    fun registerVehicle(
        brand: String,
        model: String,
        year: Int,
        plate: String,
        vehicleType: String
    ) {
        val localInfo = VehicleInfo(
            brand = brand,
            model = model,
            year = year.toString(),
            plate = plate,
            vehicleType = vehicleType,
            syncedWithServer = false
        )
        sessionRepository.saveVehicleInfo(localInfo)
        viewModelScope.launch {
            _actionState.update { it.copy(isSaving = true, message = null) }
            val result = supabaseRepository.registerVehicle(brand, model, year, plate, vehicleType)
            result.onSuccess {
                sessionRepository.saveVehicleInfo(localInfo.copy(syncedWithServer = true))
                _actionState.update {
                    it.copy(isSaving = false, message = "¡Vehículo registrado con éxito en Supabase!")
                }
            }.onFailure { err ->
                _actionState.update {
                    it.copy(
                        isSaving = false,
                        message = "Vehículo guardado en el dispositivo. Supabase: ${err.localizedMessage ?: "pendiente"}"
                    )
                }
            }
        }
    }

    fun uploadDocument(documentType: String, documentUri: String) {
        sessionRepository.markDocumentUploaded(documentType)
        viewModelScope.launch {
            _actionState.update { it.copy(isSaving = true, message = null) }
            val result = supabaseRepository.registerDocument(documentType, documentUri)
            result.onSuccess {
                val label = when (documentType) {
                    "ine" -> "¡INE registrada y en revisión!"
                    "license" -> "¡Licencia registrada y en revisión!"
                    else -> "¡Póliza de seguro registrada!"
                }
                _actionState.update { it.copy(isSaving = false, message = label) }
            }.onFailure {
                _actionState.update {
                    it.copy(
                        isSaving = false,
                        message = "Documento guardado localmente para revisión."
                    )
                }
            }
        }
    }

    fun markFaceVerified() {
        sessionRepository.markFaceVerified()
        viewModelScope.launch {
            supabaseRepository.registerDocument("face_biometric", "verified_camerax_liveness")
        }
    }

    fun markIneScanned() {
        sessionRepository.markIneScanned()
        viewModelScope.launch {
            supabaseRepository.registerDocument("ine_scan", "verified_front_back_camerax")
        }
    }

    fun selectPaymentMethod(method: PaymentMethodType) {
        sessionRepository.selectPaymentMethod(method)
        val msg = when (method) {
            PaymentMethodType.CASH -> "Efectivo seleccionado como método de pago predeterminado"
            PaymentMethodType.CARD -> "Tarjeta seleccionada como método predeterminado"
            PaymentMethodType.SPEI -> "Transferencia SPEI seleccionada"
        }
        _actionState.update { it.copy(message = msg) }
    }

    fun saveMaskedCard(alias: String, last4: String, brandLabel: String) {
        sessionRepository.saveMaskedCard(alias, last4, brandLabel)
        _actionState.update {
            it.copy(message = "Tarjeta $brandLabel •••• $last4 guardada como predeterminada")
        }
    }

    fun scheduleRide(destination: String, dateLabel: String, timeLabel: String, mode: ServiceMode, notes: String) {
        sessionRepository.addScheduledRide(destination, dateLabel, timeLabel, mode, notes)
        _actionState.update {
            it.copy(message = "Viaje programado para $dateLabel a las $timeLabel")
        }
    }

    fun cancelScheduledRide(id: String) {
        sessionRepository.removeScheduledRide(id)
        _actionState.update { it.copy(message = "Reserva cancelada") }
    }

    fun markInboxRead() {
        sessionRepository.markAllInboxRead()
    }

    fun switchRole(role: UserRole) {
        sessionRepository.selectRole(role)
        _actionState.update { it.copy(message = "Modo cambiado a ${role.displayName}") }
    }

    fun logout() {
        sessionRepository.logout()
    }

    fun clearMessage() {
        _actionState.update { it.copy(message = null) }
    }
}
