package com.example.viakaab.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface ViaKaabRoute {
    @Serializable
    data object Splash : ViaKaabRoute

    @Serializable
    data object Login : ViaKaabRoute

    @Serializable
    data object Register : ViaKaabRoute

    @Serializable
    data object RoleSelection : ViaKaabRoute

    @Serializable
    data object MainContainer : ViaKaabRoute

    @Serializable
    data class PassengerMap(
        val modeName: String = "STANDARD",
        val assistanceNote: String = ""
    ) : ViaKaabRoute

    @Serializable
    data object DriverMap : ViaKaabRoute

    @Serializable
    data object Profile : ViaKaabRoute

    @Serializable
    data object EditProfile : ViaKaabRoute

    @Serializable
    data object DriverProfile : ViaKaabRoute

    @Serializable
    data object PaymentMethods : ViaKaabRoute

    @Serializable
    data object Support : ViaKaabRoute

    @Serializable
    data object Terms : ViaKaabRoute

    @Serializable
    data object GeminiChat : ViaKaabRoute

    @Serializable
    data object FaceVerification : ViaKaabRoute

    @Serializable
    data object IneVerification : ViaKaabRoute
}
