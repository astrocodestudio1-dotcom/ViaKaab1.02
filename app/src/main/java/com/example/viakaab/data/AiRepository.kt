package com.example.viakaab.data

interface AiRepository {
    val isFirebaseConnected: Boolean
    suspend fun askAssistant(prompt: String): Result<String>
}

class PlaceholderAiRepository : AiRepository {
    override val isFirebaseConnected: Boolean = false

    override suspend fun askAssistant(prompt: String): Result<String> {
        return Result.success(
            "Respuesta de ejemplo (Firebase AI Logic pendiente de activación): " +
                "Para \"$prompt\", en Felipe Carrillo Puerto los viajes locales estándar suelen " +
                "iniciar desde $35–$50 MXN según distancia y oferta."
        )
    }
}
