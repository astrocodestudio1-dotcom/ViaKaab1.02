package com.example.viakaab.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viakaab.data.AiRepository
import com.example.viakaab.model.ChatMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeminiChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            id = "welcome",
            isUser = false,
            text = "¡Hola! Soy tu asistente de Inteligencia Artificial Gemini de ViaKaab. ¿En qué puedo ayudarte hoy con tus viajes o entregas en Felipe Carrillo Puerto?"
        )
    ),
    val isLoading: Boolean = false,
    val isFirebaseConnected: Boolean = false
)

class GeminiChatViewModel(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GeminiChatUiState(isFirebaseConnected = aiRepository.isFirebaseConnected)
    )
    val uiState: StateFlow<GeminiChatUiState> = _uiState.asStateFlow()

    fun sendPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            text = trimmed
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val result = aiRepository.askAssistant(trimmed)
                result.onSuccess { reply ->
                    val aiMsg = ChatMessage(
                        id = "ai_${System.currentTimeMillis()}",
                        isUser = false,
                        text = reply
                    )
                    _uiState.update {
                        it.copy(messages = it.messages + aiMsg, isLoading = false)
                    }
                }.onFailure { err ->
                    val errMsg = ChatMessage(
                        id = "err_${System.currentTimeMillis()}",
                        isUser = false,
                        text = err.localizedMessage ?: "No se pudo obtener respuesta de Gemini AI.",
                        isError = true
                    )
                    _uiState.update {
                        it.copy(messages = it.messages + errMsg, isLoading = false)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GeminiChatViewModel", "Error invoking AI assistant", e)
                val errMsg = ChatMessage(
                    id = "err_${System.currentTimeMillis()}",
                    isUser = false,
                    text = e.localizedMessage ?: "Error inesperado al consultar Gemini AI.",
                    isError = true
                )
                _uiState.update {
                    it.copy(messages = it.messages + errMsg, isLoading = false)
                }
            }
        }
    }
}
