package com.example.viakaab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viakaab.data.SessionRepository
import com.example.viakaab.data.SupabaseRepository
import com.example.viakaab.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val forgotPasswordDialogMessage: String? = null,
    val registerSuccessDialog: Boolean = false
)

class AuthViewModel(
    private val sessionRepository: SessionRepository,
    private val supabaseRepository: SupabaseRepository
) : ViewModel() {

    val session = sessionRepository.session

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onNavigate: (UserRole) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            val result = supabaseRepository.login(email, password)
            result.onSuccess { loginResult ->
                sessionRepository.saveCredentials(email, password)
                val role = if (loginResult.role.isNotBlank()) {
                    UserRole.fromKey(loginResult.role)
                } else {
                    sessionRepository.session.value.role
                }
                sessionRepository.createLoginSession(
                    email = email,
                    name = loginResult.name,
                    role = role,
                    token = loginResult.accessToken
                )
                if (loginResult.phone.isNotBlank()) {
                    sessionRepository.setUserPhone(loginResult.phone)
                }
                _uiState.update { it.copy(isLoading = false, statusMessage = "¡Bienvenido de vuelta a ViaKaab!") }
                onNavigate(role)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = err.localizedMessage
                            ?: "No pudimos iniciar sesión. Verifica tu correo, contraseña y conexión."
                    )
                }
            }
        }
    }

    fun register(
        name: String,
        phone: String,
        age: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            sessionRepository.setUserPhone(phone)
            val result = supabaseRepository.signUpWithRole(
                name = name,
                phone = phone,
                age = age,
                email = email,
                password = password,
                role = UserRole.PASSENGER.key
            )
            result.onSuccess {
                sessionRepository.saveCredentials(email, password)
                sessionRepository.createLoginSession(
                    email = email,
                    name = name,
                    role = UserRole.PASSENGER,
                    token = ""
                )
                _uiState.update { it.copy(isLoading = false, registerSuccessDialog = true, statusMessage = null) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = error.localizedMessage ?: "Error al registrar usuario en Supabase."
                    )
                }
            }
        }
    }

    fun dismissRegisterSuccessDialog() {
        _uiState.update { it.copy(registerSuccessDialog = false) }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            val result = supabaseRepository.resetPassword(email)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = null,
                        forgotPasswordDialogMessage = "Se ha enviado un enlace a $email para restablecer tu contraseña. Revisa tu bandeja de entrada o spam."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = error.localizedMessage ?: "No se pudo enviar el correo de recuperación."
                    )
                }
            }
        }
    }

    fun dismissForgotPasswordDialog() {
        _uiState.update { it.copy(forgotPasswordDialogMessage = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun selectRole(role: UserRole) {
        sessionRepository.selectRole(role)
    }
}
