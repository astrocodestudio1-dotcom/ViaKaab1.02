package com.example.viakaab

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.viakaab.data.GoogleMapsRepository
import com.example.viakaab.data.PlaceholderAiRepository
import com.example.viakaab.data.SessionRepository
import com.example.viakaab.data.SupabaseRepository
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.ui.navigation.ViaKaabRoute
import com.example.viakaab.ui.screens.DriverMapScreen
import com.example.viakaab.ui.screens.DriverProfileScreen
import com.example.viakaab.ui.screens.EatsMarketplaceScreen
import com.example.viakaab.ui.screens.EditProfileScreen
import com.example.viakaab.ui.screens.FaceVerificationScreen
import com.example.viakaab.ui.screens.GeminiChatScreen
import com.example.viakaab.ui.screens.IneVerificationScreen
import com.example.viakaab.ui.screens.LoginScreen
import com.example.viakaab.ui.screens.MainContainerScreen
import com.example.viakaab.ui.screens.PassengerMapScreen
import com.example.viakaab.ui.screens.PaymentMethodsScreen
import com.example.viakaab.ui.screens.ProfileScreen
import com.example.viakaab.ui.screens.RegisterScreen
import com.example.viakaab.ui.screens.RoleSelectionScreen
import com.example.viakaab.ui.screens.SplashScreen
import com.example.viakaab.ui.screens.SupportScreen
import com.example.viakaab.ui.screens.TermsScreen
import com.example.viakaab.ui.theme.ViaKaabTheme
import com.example.viakaab.viewmodel.AuthViewModel
import com.example.viakaab.viewmodel.GeminiChatViewModel
import com.example.viakaab.viewmodel.MainAppViewModel
import com.example.viakaab.viewmodel.MapViewModel

class MainActivity : ComponentActivity() {
    private var isInPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ViaKaabTheme {
                ViaKaabAppNavigation(
                    isInPipMode = isInPipMode,
                    onEnterFloatingPip = { enterFloatingPipMode() }
                )
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
    }

    private fun enterFloatingPipMode(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return runCatching {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 10))
                    .build()
                enterPictureInPictureMode(params)
            }.getOrDefault(false)
        }
        return false
    }
}

@Composable
fun ViaKaabAppNavigation(
    isInPipMode: Boolean = false,
    onEnterFloatingPip: () -> Boolean = { false }
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val sessionRepository = remember { SessionRepository(context) }
    val supabaseRepository = remember { SupabaseRepository() }
    val mapsRepository = remember { GoogleMapsRepository() }
    val aiRepository = remember { PlaceholderAiRepository() }

    val authViewModel = remember { AuthViewModel(sessionRepository, supabaseRepository) }
    val mainAppViewModel = remember { MainAppViewModel(sessionRepository, supabaseRepository) }
    val mapViewModel = remember { MapViewModel(sessionRepository, mapsRepository) }
    val geminiChatViewModel = remember { GeminiChatViewModel(aiRepository) }

    NavHost(
        navController = navController,
        startDestination = ViaKaabRoute.Splash
    ) {
        composable<ViaKaabRoute.Splash> {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(ViaKaabRoute.Login) {
                        popUpTo(ViaKaabRoute.Splash) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(ViaKaabRoute.MainContainer) {
                        popUpTo(ViaKaabRoute.Splash) { inclusive = true }
                    }
                },
                onNavigateToDriverMap = {
                    navController.navigate(ViaKaabRoute.DriverMap) {
                        popUpTo(ViaKaabRoute.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<ViaKaabRoute.Login> {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToMain = {
                    navController.navigate(ViaKaabRoute.MainContainer) {
                        popUpTo(ViaKaabRoute.Login) { inclusive = true }
                    }
                },
                onNavigateToDriverMap = {
                    navController.navigate(ViaKaabRoute.DriverMap) {
                        popUpTo(ViaKaabRoute.Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(ViaKaabRoute.Register)
                }
            )
        }

        composable<ViaKaabRoute.Register> {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToRoleSelection = {
                    navController.navigate(ViaKaabRoute.RoleSelection) {
                        popUpTo(ViaKaabRoute.Login) { inclusive = true }
                    }
                },
                onNavigateToTerms = {
                    navController.navigate(ViaKaabRoute.Terms)
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<ViaKaabRoute.RoleSelection> {
            RoleSelectionScreen(
                authViewModel = authViewModel,
                onNavigateToPassengerHome = {
                    navController.navigate(ViaKaabRoute.MainContainer) {
                        popUpTo(ViaKaabRoute.RoleSelection) { inclusive = true }
                    }
                },
                onNavigateToDriverMap = {
                    navController.navigate(ViaKaabRoute.DriverMap) {
                        popUpTo(ViaKaabRoute.RoleSelection) { inclusive = true }
                    }
                }
            )
        }

        composable<ViaKaabRoute.MainContainer> {
            MainContainerScreen(
                viewModel = mainAppViewModel,
                onOpenPassengerMap = { mode, assistanceNote ->
                    navController.navigate(
                        ViaKaabRoute.PassengerMap(
                            modeName = mode.name,
                            assistanceNote = assistanceNote
                        )
                    )
                },
                onOpenQuickDestination = { mode, destinationQuery ->
                    navController.navigate(
                        ViaKaabRoute.PassengerMap(
                            modeName = mode.name,
                            initialDestinationQuery = destinationQuery
                        )
                    )
                },
                onOpenEatsMarketplace = { categoryKey, storeId ->
                    navController.navigate(
                        ViaKaabRoute.EatsMarketplace(
                            categoryKey = categoryKey,
                            storeId = storeId
                        )
                    )
                },
                onOpenDriverMap = {
                    navController.navigate(ViaKaabRoute.DriverMap) {
                        popUpTo(ViaKaabRoute.MainContainer) { inclusive = true }
                    }
                },
                onOpenProfile = { navController.navigate(ViaKaabRoute.Profile) },
                onOpenSupport = { navController.navigate(ViaKaabRoute.Support) },
                onOpenPaymentMethods = { navController.navigate(ViaKaabRoute.PaymentMethods) },
                onOpenTerms = { navController.navigate(ViaKaabRoute.Terms) },
                onOpenGeminiChat = { navController.navigate(ViaKaabRoute.GeminiChat) },
                onOpenDriverProfile = { navController.navigate(ViaKaabRoute.DriverProfile) },
                onLogout = {
                    navController.navigate(ViaKaabRoute.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<ViaKaabRoute.PassengerMap> { backStackEntry ->
            val route = backStackEntry.toRoute<ViaKaabRoute.PassengerMap>()
            val initialMode = ServiceMode.entries.firstOrNull { it.name == route.modeName }
                ?: ServiceMode.STANDARD
            PassengerMapScreen(
                mapViewModel = mapViewModel,
                initialMode = initialMode,
                initialAssistanceNote = route.assistanceNote,
                initialDestinationQuery = route.initialDestinationQuery,
                onBack = { navController.popBackStack() },
                onOpenProfileMenu = { navController.navigate(ViaKaabRoute.Profile) }
            )
        }

        composable<ViaKaabRoute.EatsMarketplace> { backStackEntry ->
            val route = backStackEntry.toRoute<ViaKaabRoute.EatsMarketplace>()
            EatsMarketplaceScreen(
                mapViewModel = mapViewModel,
                initialCategoryKey = route.categoryKey,
                initialStoreId = route.storeId,
                onBack = { navController.popBackStack() },
                onOpenCustomDeliveryMap = {
                    navController.navigate(
                        ViaKaabRoute.PassengerMap(
                            modeName = ServiceMode.DELIVERY.name,
                            assistanceNote = "Mandado personalizado ViaKaab Eats"
                        )
                    ) {
                        popUpTo(ViaKaabRoute.MainContainer)
                    }
                },
                onOrderConfirmed = {
                    navController.navigate(
                        ViaKaabRoute.PassengerMap(
                            modeName = ServiceMode.DELIVERY.name,
                            assistanceNote = ""
                        )
                    ) {
                        popUpTo(ViaKaabRoute.MainContainer)
                    }
                }
            )
        }

        composable<ViaKaabRoute.DriverMap> {
            DriverMapScreen(
                mapViewModel = mapViewModel,
                isInPipMode = isInPipMode,
                onEnterFloatingPip = onEnterFloatingPip,
                onOpenDriverProfile = { navController.navigate(ViaKaabRoute.DriverProfile) },
                onSwitchRole = { newRole ->
                    mainAppViewModel.switchRole(newRole)
                    if (!newRole.isDriverRole) {
                        navController.navigate(ViaKaabRoute.MainContainer) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<ViaKaabRoute.Profile> {
            ProfileScreen(
                viewModel = mainAppViewModel,
                onBack = { navController.popBackStack() },
                onOpenDriverProfile = { navController.navigate(ViaKaabRoute.DriverProfile) },
                onOpenEditProfile = { navController.navigate(ViaKaabRoute.EditProfile) },
                onOpenPaymentMethods = { navController.navigate(ViaKaabRoute.PaymentMethods) },
                onOpenSupport = { navController.navigate(ViaKaabRoute.Support) },
                onSwitchRole = { newRole ->
                    mainAppViewModel.switchRole(newRole)
                    if (newRole.isDriverRole) {
                        navController.navigate(ViaKaabRoute.DriverMap) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ViaKaabRoute.MainContainer) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onLogout = {
                    navController.navigate(ViaKaabRoute.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<ViaKaabRoute.EditProfile> {
            EditProfileScreen(
                viewModel = mainAppViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ViaKaabRoute.DriverProfile> {
            DriverProfileScreen(
                viewModel = mainAppViewModel,
                onBack = { navController.popBackStack() },
                onOpenEditProfile = { navController.navigate(ViaKaabRoute.EditProfile) },
                onOpenSupport = { navController.navigate(ViaKaabRoute.Support) },
                onOpenFaceVerification = { navController.navigate(ViaKaabRoute.FaceVerification) },
                onOpenIneVerification = { navController.navigate(ViaKaabRoute.IneVerification) },
                onSwitchRole = { newRole ->
                    mainAppViewModel.switchRole(newRole)
                    if (newRole.isDriverRole) {
                        navController.navigate(ViaKaabRoute.DriverMap) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ViaKaabRoute.MainContainer) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onLogout = {
                    navController.navigate(ViaKaabRoute.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<ViaKaabRoute.PaymentMethods> {
            PaymentMethodsScreen(
                viewModel = mainAppViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ViaKaabRoute.Support> {
            SupportScreen(
                viewModel = mainAppViewModel,
                onBack = { navController.popBackStack() },
                onOpenTerms = { navController.navigate(ViaKaabRoute.Terms) }
            )
        }

        composable<ViaKaabRoute.Terms> {
            TermsScreen(onBack = { navController.popBackStack() })
        }

        composable<ViaKaabRoute.GeminiChat> {
            GeminiChatScreen(
                viewModel = geminiChatViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ViaKaabRoute.FaceVerification> {
            FaceVerificationScreen(
                onVerified = {
                    mainAppViewModel.markFaceVerified()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<ViaKaabRoute.IneVerification> {
            IneVerificationScreen(
                onVerified = {
                    mainAppViewModel.markIneScanned()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
