package com.example.viakaab.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.model.DriverFeedFilter
import com.example.viakaab.model.DriverRequestCategory
import com.example.viakaab.model.DriverRideRequest
import com.example.viakaab.model.DriverTripStage
import com.example.viakaab.model.UserRole
import com.example.viakaab.ui.components.SafetyAndSosDialog
import com.example.viakaab.ui.components.ViaKaabInteractiveMap
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.viewmodel.MapViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

@SuppressLint("MissingPermission")
@Composable
fun DriverMapScreen(
    mapViewModel: MapViewModel,
    isInPipMode: Boolean = false,
    onEnterFloatingPip: () -> Boolean = { false },
    onOpenDriverProfile: () -> Unit,
    onSwitchRole: (UserRole) -> Unit = {}
) {
    val context = LocalContext.current
    val session by mapViewModel.session.collectAsStateWithLifecycle()
    val driverState by mapViewModel.driverState.collectAsStateWithLifecycle()
    val onlineUnits by mapViewModel.onlineUnits.collectAsStateWithLifecycle()
    val supportPhone by mapViewModel.supportPhone.collectAsStateWithLifecycle()
    val trustedContactPhone by mapViewModel.trustedContactPhone.collectAsStateWithLifecycle()
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun fetchDriverLocation() {
        if (!hasLocationPermission) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val bearing = if (location.hasBearing()) location.bearing else null
                mapViewModel.onDriverLocationResolved(location.latitude, location.longitude, bearing)
                googleMapRef?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        15.6f
                    )
                )
            }
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L)
                .setMinUpdateIntervalMillis(2500L)
                .build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    val bearing = if (loc.hasBearing()) loc.bearing else null
                    mapViewModel.onDriverLocationResolved(loc.latitude, loc.longitude, bearing)
                }
            }
            runCatching {
                fusedLocationClient.requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                )
            }
            onDispose {
                runCatching { fusedLocationClient.removeLocationUpdates(callback) }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            runCatching { googleMapRef?.isMyLocationEnabled = true }
            fetchDriverLocation()
        }
    }

    var showAcceptDialog by rememberSaveable { mutableStateOf(false) }
    var showCounterDialog by rememberSaveable { mutableStateOf(false) }
    var showSosDialog by rememberSaveable { mutableStateOf(false) }
    var showQuickRoleDialog by rememberSaveable { mutableStateOf(false) }
    var isFloatingMinimized by rememberSaveable { mutableStateOf(false) }
    var counterAmount by rememberSaveable { mutableStateOf("") }
    var floatBubbleOffsetX by remember { mutableFloatStateOf(0f) }
    var floatBubbleOffsetY by remember { mutableFloatStateOf(0f) }
    var floatCardOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(showAcceptDialog, showCounterDialog, showQuickRoleDialog) {
        mapViewModel.setDriverAutoRefreshPaused(
            showAcceptDialog || showCounterDialog || showQuickRoleDialog
        )
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchDriverLocation()
        }
    }

    LaunchedEffect(driverState.toastMessage) {
        driverState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mapViewModel.clearDriverToast()
        }
    }

    val visibleRequests = driverState.visibleRequestsFor(session.role)
    val req = driverState.activeRequestFor(session.role)

    if (isInPipMode) {
        DriverPipFloatingView(
            isOnline = driverState.isOnline,
            role = session.role,
            request = req,
            tripStage = driverState.tripStage,
            secondsLeft = driverState.autoCycleSecondsLeft
        )
        return
    }

    val headerText = when (session.role) {
        UserRole.DELIVERY -> "Pedidos de comida, farmacia, súper y envíos"
        UserRole.BOTH -> "Pasajeros, comida y envíos en Carrillo Puerto"
        else -> "Pasajeros solicitando viaje cerca de ti"
    }

    if (showQuickRoleDialog) {
        AlertDialog(
            onDismissRequest = { showQuickRoleDialog = false },
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Cambiar perfil operativo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Elige cómo quieres operar ahora mismo:",
                        color = Slate400,
                        fontSize = 13.sp
                    )
                    UserRole.entries.forEach { role ->
                        val active = role == session.role
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (active) BrandEmerald.copy(alpha = 0.2f) else Slate800
                            ),
                            border = BorderStroke(1.dp, if (active) BrandEmerald else Slate700),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showQuickRoleDialog = false
                                    onSwitchRole(role)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = role.displayName,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (active) {
                                    Text("Activo", color = BrandEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickRoleDialog = false }) {
                    Text("Cerrar", color = BrandSkyTint)
                }
            }
        )
    }

    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = {
                Text(
                    text = if (req.isDeliveryOrCommerce) "¡Pedido Aceptado!" else "¡Viaje Aceptado!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val extra = req.orderDetails.takeIf { it.isNotBlank() }?.let { "\n\nDetalle: $it" }.orEmpty()
                Text(
                    "Has aceptado: ${req.origin} ➔ ${req.destination} por $${req.fareMxn} MXN.$extra\n\n" +
                        if (req.isDeliveryOrCommerce) {
                            "Dirígete al comercio o punto de recolección."
                        } else {
                            "Dirígete al punto de salida del pasajero."
                        }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showAcceptDialog = false
                    mapViewModel.startDriverRoute()
                }) {
                    Text("Iniciar Ruta", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val ratingTarget = driverState.pendingPassengerRatingTarget
    if (ratingTarget != null) {
        var selectedStars by rememberSaveable(ratingTarget.id) { mutableIntStateOf(5) }
        var ratingComment by rememberSaveable(ratingTarget.id) { mutableStateOf("") }
        val raterTitle = when {
            ratingTarget.isDeliveryOrCommerce || session.role == UserRole.DELIVERY ->
                "Calificar al cliente (Repartidor)"
            else -> "Calificar al pasajero"
        }
        AlertDialog(
            onDismissRequest = mapViewModel::dismissPassengerRating,
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text(raterTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${ratingTarget.passengerName} · ${ratingTarget.origin} ➔ ${ratingTarget.destination}",
                        color = Slate300,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { selectedStars = star },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("rate_passenger_star_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= selectedStars) {
                                        Icons.Default.Star
                                    } else {
                                        Icons.Default.StarBorder
                                    },
                                    contentDescription = "$star estrellas",
                                    tint = if (star <= selectedStars) BrandEmerald else Slate400,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ratingComment,
                        onValueChange = { ratingComment = it },
                        label = { Text("Comentario opcional (ej. Muy puntual)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = BrandEmerald,
                            unfocusedBorderColor = Slate700,
                            focusedLabelColor = BrandEmerald,
                            unfocusedLabelColor = Slate400
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { mapViewModel.submitPassengerRating(selectedStars, ratingComment) },
                    modifier = Modifier.testTag("submit_passenger_rating_button")
                ) {
                    Text("Enviar ★ $selectedStars", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = mapViewModel::dismissPassengerRating) {
                    Text("Omitir", color = Slate400)
                }
            }
        )
    }

    if (showSosDialog) {
        val reqSummary = "🛡️ *ViaKaab ${session.role.displayName} · Recorrido activo*\n" +
            "Operador: ${session.effectiveDisplayName}\n" +
            "Ruta: ${req.origin} ➔ ${req.destination}\n" +
            "Tarifa: $${req.fareMxn} MXN\n" +
            "📍 Punto actual: https://maps.google.com/?q=${"%.6f".format(req.startLat)},${"%.6f".format(req.startLng)}"
        SafetyAndSosDialog(
            supportPhone = supportPhone,
            trustedContactPhone = trustedContactPhone,
            shareMessage = reqSummary,
            onSaveSupportPhone = mapViewModel::saveSupportPhone,
            onSaveTrustedContactPhone = mapViewModel::saveTrustedContactPhone,
            onDismiss = { showSosDialog = false }
        )
    }

    if (showCounterDialog) {
        AlertDialog(
            onDismissRequest = { showCounterDialog = false },
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Contraofertar Tarifa", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ingresa tu contraoferta en $ MXN:", color = Slate300)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = counterAmount,
                        onValueChange = { counterAmount = it },
                        label = { Text("Monto oferta (ej. 50)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = BrandEmerald,
                            unfocusedBorderColor = Slate700,
                            focusedLabelColor = BrandEmerald,
                            unfocusedLabelColor = Slate400
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = counterAmount.trim()
                    if (trimmed.toIntOrNull() != null) {
                        mapViewModel.sendCounterOffer(trimmed)
                        counterAmount = ""
                        showCounterDialog = false
                    } else {
                        Toast.makeText(context, "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Enviar Oferta", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCounterDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ViaKaabInteractiveMap(
            pickupLatLng = LatLng(req.startLat, req.startLng),
            pickupTitle = if (req.isDeliveryOrCommerce) "Recoger: ${req.origin.take(24)}" else "Salida: ${req.origin.take(24)}",
            pickupSnippet = "${req.passengerName} ($${req.fareMxn} MXN)",
            destinationLatLng = LatLng(req.endLat, req.endLng),
            destinationTitle = "Entrega/Destino: ${req.destination}",
            destinationSnippet = req.serviceTypeLabel,
            routeColor = if (req.isDeliveryOrCommerce) BrandBlue else BrandEmerald,
            hasLocationPermission = hasLocationPermission,
            landmarks = mapViewModel.quickLandmarks,
            onlineUnits = onlineUnits,
            driverLatLng = if (driverState.tripStage != DriverTripStage.OFFER_AVAILABLE) {
                driverState.driverLatLng
            } else {
                null
            },
            driverLabel = session.effectiveDisplayName,
            isSearchingDrivers = false,
            etaMinutes = if (driverState.tripStage != DriverTripStage.OFFER_AVAILABLE) 4 else null,
            bottomOverlayReserveDp = 310.dp,
            badgeTopPaddingDp = 108.dp,
            onGoogleMapReady = { googleMapRef = it },
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar: Menu, Dynamic Role Switcher + Earnings, Online Switch
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDriverProfile,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate900.copy(alpha = 0.90f))
                        .testTag("driver_menu_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú Conductor", tint = PureWhite)
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate900.copy(alpha = 0.90f))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$${driverState.earningsTodayMxn} MXN",
                        color = BrandEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${driverState.completedTripsToday} completados hoy",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate900.copy(alpha = 0.90f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (driverState.isOnline) "En línea" else "Offline",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = driverState.isOnline,
                        onCheckedChange = mapViewModel::toggleDriverOnline,
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = BrandEmerald),
                        modifier = Modifier.testTag("driver_online_switch")
                    )
                }
            }

            // Dynamic active role badge (tap to switch between Conductor / Repartidor / Ambos / Pasajero)
            Surface(
                shape = RoundedCornerShape(50),
                color = Slate900.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.5f)),
                modifier = Modifier
                    .clickable { showQuickRoleDialog = true }
                    .testTag("driver_role_switcher_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (session.role) {
                            UserRole.DELIVERY -> Icons.Default.TwoWheeler
                            else -> Icons.Default.DirectionsCar
                        },
                        contentDescription = null,
                        tint = BrandEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Perfil: ${session.role.displayName}",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Cambiar perfil",
                        tint = BrandSkyTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Floating Window / Draggable Bubble + SOS & MyLocation FAB
        val activeAccent = categoryAccent(req.requestCategory)
        val livePulseTransition = rememberInfiniteTransition(label = "driver_live_pulse")
        val liveDotAlpha by livePulseTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "driver_live_dot"
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showSosDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandRed,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("driver_sos_button")
                ) {
                    Icon(Icons.Default.Emergency, contentDescription = "SOS Policía", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SOS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                IconButton(
                    onClick = {
                        if (hasLocationPermission) {
                            fetchDriverLocation()
                            val target = driverState.driverLatLng
                            if (target != null && googleMapRef != null) {
                                googleMapRef?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(target, 15.6f)
                                )
                            } else if (target == null) {
                                Toast.makeText(
                                    context,
                                    "Obteniendo señal GPS en tiempo real...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate900.copy(alpha = 0.88f))
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar mapa", tint = PureWhite)
                }
            }

            AnimatedContent(
                targetState = isFloatingMinimized,
                label = "driver_floating_window_state"
            ) { minimized ->
                if (minimized) {
                    // Draggable compact floating bubble over the map
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Slate900.copy(alpha = 0.96f),
                            shadowElevation = 16.dp,
                            border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = floatBubbleOffsetX.roundToInt(),
                                        y = floatBubbleOffsetY.roundToInt()
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        floatBubbleOffsetX = (floatBubbleOffsetX + dragAmount.x).coerceIn(-520f, 40f)
                                        floatBubbleOffsetY = (floatBubbleOffsetY + dragAmount.y).coerceIn(-900f, 20f)
                                    }
                                }
                                .clickable { isFloatingMinimized = false }
                                .testTag("driver_floating_bubble")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(
                                            (if (driverState.isOnline) BrandEmerald else Slate400)
                                                .copy(alpha = if (driverState.isOnline) liveDotAlpha else 0.8f)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = categoryIcon(req.requestCategory),
                                    contentDescription = null,
                                    tint = activeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (driverState.isOnline) {
                                            "$${req.fareMxn} MXN · ${req.origin.take(16)}"
                                        } else {
                                            "Fuera de servicio"
                                        },
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (driverState.isOnline) {
                                            "${visibleRequests.size} activas · Act. ${driverState.autoCycleSecondsLeft}s"
                                        } else {
                                            "Toca para abrir ventana"
                                        },
                                        color = BrandSkyTint,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.Default.OpenInFull,
                                    contentDescription = "Expandir ventana flotante",
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Elevated floating window card over the map (draggable vertically + PiP + minimize)
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = Slate900.copy(alpha = 0.96f),
                        shadowElevation = 18.dp,
                        border = BorderStroke(
                            1.2.dp,
                            if (driverState.isOnline) activeAccent.copy(alpha = 0.55f) else Slate700
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(0, floatCardOffsetY.roundToInt()) }
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("driver_floating_window")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 6.dp)
                                    .width(38.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Slate700)
                            )
                            // Drag handle & floating window controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            floatCardOffsetY = (floatCardOffsetY + dragAmount.y)
                                                .coerceIn(-260f, 30f)
                                        }
                                    }
                                    .padding(bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            (if (driverState.isOnline) BrandEmerald else Slate400)
                                                .copy(alpha = if (driverState.isOnline) liveDotAlpha else 0.7f)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (driverState.isOnline) {
                                        driverState.feedLastUpdatedLabel
                                    } else {
                                        "Ventana flotante · Offline"
                                    },
                                    color = if (driverState.isOnline) BrandEmerald else Slate400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (driverState.isOnline && driverState.tripStage == DriverTripStage.OFFER_AVAILABLE) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Slate800
                                    ) {
                                        Text(
                                            text = "Auto ${driverState.autoCycleSecondsLeft}s",
                                            color = BrandSkyTint,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = mapViewModel::refreshDriverFeedNow,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("driver_refresh_feed_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Actualizar solicitudes ahora",
                                            tint = BrandSkyTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        if (!onEnterFloatingPip()) {
                                            isFloatingMinimized = true
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("driver_pip_mode_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPictureAlt,
                                        contentDescription = "Pantalla flotante PiP",
                                        tint = Slate300,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { isFloatingMinimized = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("driver_minimize_floating_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Minimizar a burbuja flotante",
                                        tint = Slate300,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (driverState.isOnline && driverState.tripStage == DriverTripStage.OFFER_AVAILABLE) {
                                val cycleRatio = (driverState.autoCycleSecondsLeft.toFloat() /
                                    driverState.autoCycleTotalSeconds.coerceAtLeast(1).toFloat())
                                    .coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { cycleRatio },
                                    color = activeAccent,
                                    trackColor = Slate800,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = if (!driverState.isOnline) "Modo fuera de servicio" else headerText,
                                color = PureWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (!driverState.isOnline) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Slate800),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Activa el interruptor 'En línea' arriba para recibir viajes de pasajeros y pedidos de comida, farmacia o súper.",
                                            color = Slate400,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                if (session.role == UserRole.BOTH && driverState.tripStage == DriverTripStage.OFFER_AVAILABLE) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DriverFeedFilter.entries.forEach { filter ->
                                            val selected = driverState.selectedFeedFilter == filter
                                            FilterChip(
                                                selected = selected,
                                                onClick = { mapViewModel.setDriverFeedFilter(filter) },
                                                label = { Text(filter.label, fontSize = 12.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = Slate800,
                                                    labelColor = Slate300,
                                                    selectedContainerColor = BrandEmerald,
                                                    selectedLabelColor = PureWhite
                                                )
                                            )
                                        }
                                    }
                                }

                                if (driverState.tripStage == DriverTripStage.OFFER_AVAILABLE && visibleRequests.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(bottom = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        visibleRequests.forEach { candidate ->
                                            val isCurrent = candidate.id == req.id
                                            val accent = categoryAccent(candidate.requestCategory)
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (isCurrent) accent.copy(alpha = 0.22f) else Slate800,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isCurrent) accent else Slate700
                                                ),
                                                modifier = Modifier
                                                    .clickable { mapViewModel.selectDriverRequest(candidate.id) }
                                                    .testTag("driver_queue_item_${candidate.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = categoryIcon(candidate.requestCategory),
                                                        contentDescription = null,
                                                        tint = accent,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(
                                                            text = candidate.origin.take(18),
                                                            color = PureWhite,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "$${candidate.fareMxn} MXN · ${candidate.distanceKm}",
                                                            color = accent,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Slate800),
                                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = activeAccent.copy(alpha = 0.18f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = categoryIcon(req.requestCategory),
                                                        contentDescription = null,
                                                        tint = activeAccent,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = req.requestCategory.label,
                                                        color = activeAccent,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "$${req.fareMxn} MXN",
                                                color = BrandEmerald,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }

                                        Text(
                                            text = "${req.origin} ➔ ${req.destination}",
                                            color = PureWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${req.passengerName} (${req.passengerRatingLabel}) · ${req.serviceTypeLabel} · ${req.distanceKm}",
                                            color = BrandSkyTint,
                                            fontSize = 12.sp
                                        )

                                        if (req.orderDetails.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Slate900.copy(alpha = 0.7f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Pedido: ${req.orderDetails}",
                                                    color = Slate300,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        when (driverState.tripStage) {
                                            DriverTripStage.OFFER_AVAILABLE -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = { showAcceptDialog = true },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = BrandEmerald,
                                                            contentColor = PureWhite
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier
                                                            .weight(1.15f)
                                                            .height(48.dp)
                                                            .testTag("driver_accept_trip_button")
                                                    ) {
                                                        Text(
                                                            text = "Aceptar $${req.fareMxn}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }

                                                    OutlinedButton(
                                                        onClick = { showCounterDialog = true },
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier
                                                            .weight(0.95f)
                                                            .height(48.dp)
                                                            .testTag("driver_counter_offer_button")
                                                    ) {
                                                        Text(
                                                            text = "Ofertar",
                                                            color = PureWhite,
                                                            fontSize = 13.sp,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }

                                                    OutlinedButton(
                                                        onClick = mapViewModel::skipDriverRequest,
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.height(48.dp)
                                                    ) {
                                                        Text(
                                                            text = "Omitir",
                                                            color = Slate400,
                                                            fontSize = 12.sp,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                }
                                            }

                                            DriverTripStage.HEADING_TO_PICKUP -> {
                                                Button(
                                                    onClick = mapViewModel::advanceDriverTripStage,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = BrandBlue,
                                                        contentColor = PureWhite
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                ) {
                                                    Text(
                                                        text = if (req.isDeliveryOrCommerce) {
                                                            "Pedido Recogido · Ir a Entregar"
                                                        } else {
                                                            "Pasajero a Bordo · Iniciar Viaje"
                                                        },
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            DriverTripStage.TRIP_IN_PROGRESS -> {
                                                Button(
                                                    onClick = mapViewModel::advanceDriverTripStage,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = BrandEmerald,
                                                        contentColor = PureWhite
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                ) {
                                                    Text(
                                                        text = if (req.isDeliveryOrCommerce) {
                                                            "Entregar Pedido y Cobrar $${req.fareMxn} MXN"
                                                        } else {
                                                            "Finalizar Viaje y Cobrar $${req.fareMxn} MXN"
                                                        },
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverPipFloatingView(
    isOnline: Boolean,
    role: UserRole,
    request: DriverRideRequest,
    tripStage: DriverTripStage,
    secondsLeft: Int
) {
    val accent = categoryAccent(request.requestCategory)
    Surface(
        color = Slate900,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) BrandEmerald else Slate400)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "ViaKaab ${role.displayName}" else "Offline",
                    color = BrandEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isOnline && tripStage == DriverTripStage.OFFER_AVAILABLE) {
                    Text(
                        text = "${secondsLeft}s",
                        color = BrandSkyTint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = "$${request.fareMxn} MXN · ${request.requestCategory.label}",
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${request.origin} ➔ ${request.destination}",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = when (tripStage) {
                    DriverTripStage.OFFER_AVAILABLE -> "${request.passengerName} · ${request.distanceKm}"
                    DriverTripStage.HEADING_TO_PICKUP -> "En camino al punto de recogida"
                    DriverTripStage.TRIP_IN_PROGRESS -> "Servicio en curso"
                },
                color = Slate300,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun categoryIcon(category: DriverRequestCategory): ImageVector = when (category) {
    DriverRequestCategory.PASSENGER_RIDE -> Icons.Default.DirectionsCar
    DriverRequestCategory.FOOD_ORDER -> Icons.Default.Restaurant
    DriverRequestCategory.PHARMACY_ORDER -> Icons.Default.LocalPharmacy
    DriverRequestCategory.SUPERMARKET_ORDER -> Icons.Default.ShoppingCart
    DriverRequestCategory.PARCEL_DELIVERY -> Icons.Default.Inventory2
}

private fun categoryAccent(category: DriverRequestCategory): Color = when (category) {
    DriverRequestCategory.PASSENGER_RIDE -> BrandEmerald
    DriverRequestCategory.FOOD_ORDER -> BrandEmerald
    DriverRequestCategory.PHARMACY_ORDER -> BrandBlue
    DriverRequestCategory.SUPERMARKET_ORDER -> BrandSky
    DriverRequestCategory.PARCEL_DELIVERY -> BrandSkyTint
}
