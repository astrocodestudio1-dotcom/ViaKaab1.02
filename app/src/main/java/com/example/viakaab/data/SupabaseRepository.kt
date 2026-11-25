package com.example.viakaab.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class SupabaseLoginResult(
    val accessToken: String,
    val name: String,
    val role: String,
    val phone: String
)

class SupabaseRepository {

    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun login(email: String, password: String): Result<SupabaseLoginResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cleanEmail = email.trim().lowercase()
                val cleanPassword = password.trim()
                val bodyJson = JSONObject().apply {
                    put("email", cleanEmail)
                    put("password", cleanPassword)
                }

                val request = Request.Builder()
                    .url("$SUPABASE_URL/auth/v1/token?grant_type=password")
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        error(parseErrorMessage(responseBody))
                    }
                    val json = JSONObject(responseBody)
                    val token = json.optString("access_token", "")
                    val userObj = json.optJSONObject("user")
                    val meta = userObj?.optJSONObject("user_metadata")
                    val name = meta?.optString("full_name", meta.optString("name", "")).orEmpty()
                    val role = meta?.optString("role", "passenger").orEmpty()
                    val phone = meta?.optString("phone", "").orEmpty()
                    SupabaseLoginResult(token, name, role, phone)
                }
            }
        }

    suspend fun signUpWithRole(
        name: String,
        phone: String,
        age: String,
        email: String,
        password: String,
        role: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanEmail = email.trim().lowercase()
            val cleanPassword = password.trim()

            val userData = JSONObject().apply {
                put("name", name)
                put("full_name", name)
                put("display_name", name)
                put("phone", phone)
                put("age", age)
                put("role", role)
                put("ine_image_url", "https://viakaab.storage/ine_verified.jpg")
                put("face_image_url", "https://viakaab.storage/face_verified.jpg")
                put("verified", true)
            }

            val bodyJson = JSONObject().apply {
                put("email", cleanEmail)
                put("password", cleanPassword)
                put("data", userData)
            }

            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/signup")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    return@use responseBody
                }
                val errorMsg = parseErrorMessage(responseBody)
                if (errorMsg.contains("database error", ignoreCase = true) ||
                    errorMsg.contains("saving new user", ignoreCase = true)
                ) {
                    return@use signUpSimple(cleanEmail, cleanPassword)
                }
                error(errorMsg)
            }
        }
    }

    private fun signUpSimple(email: String, password: String): String {
        val bodyJson = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val request = Request.Builder()
            .url("$SUPABASE_URL/auth/v1/signup")
            .addHeader("apikey", SUPABASE_KEY)
            .addHeader("Authorization", "Bearer $SUPABASE_KEY")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error(parseErrorMessage(responseBody))
            }
            return responseBody
        }
    }

    suspend fun updateProfile(email: String, fullName: String, phone: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bodyJson = JSONObject().apply {
                    put("full_name", fullName)
                    put("phone", phone)
                }
                val request = Request.Builder()
                    .url("$SUPABASE_URL/rest/v1/profiles?email=eq.$email")
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .patch(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                executeSimple(request)
            }
        }

    suspend fun registerVehicle(
        brand: String,
        model: String,
        year: Int,
        plate: String,
        vehicleType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bodyJson = JSONObject().apply {
                put("brand", brand)
                put("model", model)
                put("year", year)
                put("plate", plate)
                put("vehicle_type", vehicleType)
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/vehicles")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            executeSimple(request)
        }
    }

    suspend fun registerDocument(documentType: String, documentUrl: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bodyJson = JSONObject().apply {
                    put("document_type", documentType)
                    put("document_url", documentUrl)
                    put("status", "pending")
                }
                val request = Request.Builder()
                    .url("$SUPABASE_URL/rest/v1/documents")
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                executeSimple(request)
            }
        }

    suspend fun resetPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bodyJson = JSONObject().apply {
                put("email", email)
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/recover")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            executeSimple(request)
        }
    }

    private fun executeSimple(request: Request): String {
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error(parseErrorMessage(responseBody))
            }
            return responseBody
        }
    }

    private fun parseErrorMessage(responseBody: String): String {
        try {
            val json = JSONObject(responseBody)
            val message = when {
                json.has("error_description") -> json.getString("error_description")
                json.has("msg") -> json.getString("msg")
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> ""
            }
            val lower = message.lowercase()
            return when {
                "rate limit" in lower -> "Supabase ha alcanzado el límite temporal de correos por hora. Espera unos minutos e inténtalo de nuevo."
                "not confirmed" in lower -> "Correo electrónico no confirmado en Supabase. Revisa tu bandeja de entrada o desactiva 'Confirm Email' en Supabase."
                "invalid login credentials" in lower || "invalid_credentials" in lower -> "Credenciales incorrectas. Revisa tu correo y contraseña."
                "already registered" in lower -> "Este correo electrónico ya está registrado en Supabase."
                message.isNotBlank() -> message
                else -> "Error al comunicar con la base de datos de Supabase."
            }
        } catch (_: Exception) {
            return "Error al comunicar con la base de datos de Supabase."
        }
    }

    companion object {
        private const val SUPABASE_URL = "https://fuzdocdzcdixlocrwqeu.supabase.co"
        private const val SUPABASE_KEY = "sb_publishable_dugkzB4UL66ABOEv_AJnIg_IafCfide"
    }
}
