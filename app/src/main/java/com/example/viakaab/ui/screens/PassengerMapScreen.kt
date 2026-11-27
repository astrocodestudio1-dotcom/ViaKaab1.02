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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.model.ActiveLiveTrip
import com.example.viakaab.model.LiveTripPhase
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.ui.components.LiveTripTrackingCard
import com.example.viakaab.ui.components.SafetyAndSosDialog
import com.example.viakaab.ui.components.ViaKaabInteractiveMap
import com.example.viakaab.ui.components.shareRouteViaWhatsApp
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
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
import kotlin.math.roundToInt

@SuppressLint("MissingPermission")
@Composable
fun PassengerMapScreen(
    mapViewModel: MapViewModel,
    initialMode: ServiceMode = ServiceMode.STANDARD,
    initialAssistanceNote: String = "",
    initialDestinationQuery: String = "",
    isInPipMode: Boolean = false,
    onEnterFloatingPip: () -> Boolean = { false },
    onBack: () -> Unit,
    onOpenProfileMenu: () -> Unit
) {
    val context = LocalContext.current
    val uiState by mapViewModel.passengerState.collectAsStateWithLifecycle()
    val onlineUnits by mapViewModel.onlineUnits.collectAsStateWithLifecycle()
    val supportPhone by mapViewModel.supportPhone.collectAsStateWithLifecycle()
    val trustedContactPhone by mapViewModel.trustedContactPhone.collectAsStateWithLifecycle()
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showSosDialog by rememberSaveable { mutableStateOf(false) }
    var isFloatingMinimized by rememberSaveable { mutableStateOf(false) }
    var floatBubbleOffsetX by remember { mutableFloatStateOf(0f) }
    var floatBubbleOffsetY by remember { mutableFloatStateOf(0f) }
    var floatCardOffsetY by remember { mutableFloatStateOf(0f) }
    var recenterTrigger by remember { mutableIntStateOf(0) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(initialMode, initialAssistanceNote, initialDestinationQuery) {
        mapViewModel.applyEntryContext(initialMode, initialAssistanceNote, initialDestinationQuery)
    }

    fun fetchLocation() {
        if (!hasLocationPermission) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                mapViewModel.onUserLocationResolved(location.latitude, location.longitude)
                googleMapRef?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        16f
                    )
                )
            } else {
                mapViewModel.onUserLocationResolved(19.5775, -88.0453)
            }
        }
    }

    // Stream real-time GPS updates while PassengerMapScreen is active and permitted
    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(3000L)
                .build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    mapViewModel.onUserLocationResolved(loc.latitude, loc.longitude)
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
            fetchLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocation()
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mapViewModel.clearPassengerToast()
        }
    }

    if (showSosDialog) {
        SafetyAndSosDialog(
            supportPhone = supportPhone,
            trustedContactPhone = trustedContactPhone,
            shareMessage = mapViewModel.buildWhatsAppShareMessage(),
            onSaveSupportPhone = mapViewModel::saveSupportPhone,
            onSaveTrustedContactPhone = mapViewModel::saveTrustedContactPhone,
            onDismiss = { showSosDialog = false }
        )
    }

    if (showConfirmDialog) {
        val title = when (uiState.selectedMode) {
            ServiceMode.DELIVERY -> "Confirmar Envío de Paquete"
            ServiceMode.INTERCITY -> "Confirmar Viaje Interurbano"
            ServiceMode.STANDARD -> "Confirmar Viaje Local"
        }

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Slate800,
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = BrandEmerald, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(uiState.pickupText, color = Slate300, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = BrandRed, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(uiState.destinationText, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }

                    uiState.estimatedDistanceKm?.let { dist ->
                        Text(
                            text = "Distancia estimada: ${"%.1f".format(dist)} km",
                            color = BrandSkyTint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (uiState.assistanceNote.isNotBlank()) {
                        Text("Nota: ${uiState.assistanceNote}", color = Slate300, fontSize = 12.sp)
                    }
                    if (uiState.selectedMode == ServiceMode.INTERCITY) {
                        val seats = uiState.seatsText.ifBlank { "1" }
                        Text("Pasajeros/Asientos: $seats", color = Slate300, fontSize = 12.sp)
                    } else if (uiState.selectedMode == ServiceMode.DELIVERY) {
                        if (uiState.packageDetailsText.isNotBlank()) {
                            Text("Paquete: ${uiState.packageDetailsText}", color = Slate300, fontSize = 12.sp)
                        }
                        if (uiState.recipientText.isNotBlank()) {
                            Text("Recibe: ${uiState.recipientText}", color = Slate300, fontSize = 12.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandEmerald.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tu oferta de tarifa", color = Slate300, fontSize = 13.sp)
                            Text("$${uiState.fareOfferText} MXN", color = BrandEmerald, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        }
                    }
                    Text(
                        text = "¿Deseas publicar esta oferta y seguir el viaje en tiempo real?",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    mapViewModel.publishTripOffer()
                }) {
                    Text("Publicar Oferta", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    val liveTrip = uiState.activeLiveTrip

    if (isInPipMode) {
        PassengerPipFloatingView(
            liveTrip = liveTrip,
            selectedMode = uiState.selectedMode,
            destinationText = uiState.destinationText,
            fareOfferText = uiState.fareOfferText
        )
        return
    }

    val activeDriverLatLng = liveTrip?.let {
        if (it.phase == LiveTripPhase.DRIVER_ARRIVING || it.phase == LiveTripPhase.IN_PROGRESS) {
            LatLng(it.driverLat, it.driverLng)
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        ViaKaabInteractiveMap(
            pickupLatLng = liveTrip?.let { LatLng(it.pickupLat, it.pickupLng) } ?: uiState.userLatLng,
            pickupTitle = if (liveTrip != null) {
                liveTrip.origin.take(26)
            } else if (uiState.isUsingRealTimeLocation) {
                "Recogida · En vivo"
            } else {
                "Origen"
            },
            pickupSnippet = uiState.lastGpsUpdatedLabel,
            destinationLatLng = liveTrip?.let { LatLng(it.destLat, it.destLng) } ?: uiState.destinationLatLng,
            destinationTitle = liveTrip?.destination
                ?: uiState.destinationMarkerTitle.ifBlank { uiState.destinationText },
            destinationSnippet = "Punto de llegada de viaje / entrega",
            routeColor = BrandBlue,
            routePolylinePoints = uiState.streetRoutePoints,
            hasLocationPermission = hasLocationPermission,
            landmarks = mapViewModel.quickLandmarks,
            onlineUnits = onlineUnits,
            driverLatLng = activeDriverLatLng,
            driverLabel = liveTrip?.driverName ?: "Unidad ViaKaab",
            isSearchingDrivers = liveTrip?.phase == LiveTripPhase.SEARCHING,
            etaMinutes = liveTrip?.etaMinutes,
            topOverlayReserveDp = 120.dp,
            bottomOverlayReserveDp = when {
                isFloatingMinimized -> 132.dp
                liveTrip != null && uiState.isLiveTripPanelExpanded -> 340.dp
                else -> 430.dp
            },
            badgeTopPaddingDp = 82.dp,
            recenterTrigger = recenterTrigger,
            onMapTap = mapViewModel::onMapTapped,
            onGoogleMapReady = { googleMapRef = it },
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar: Back + Menu on left, WhatsApp + SOS on right
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate900.copy(alpha = 0.90f))
                        .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                        .testTag("passenger_map_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = PureWhite)
                }
                IconButton(
                    onClick = onOpenProfileMenu,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate900.copy(alpha = 0.90f))
                        .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                        .testTag("passenger_map_menu_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú", tint = PureWhite)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        shareRouteViaWhatsApp(
                            context = context,
                            trustedPhone = trustedContactPhone,
                            message = mapViewModel.buildWhatsAppShareMessage()
                        )
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BrandEmerald.copy(alpha = 0.92f))
                        .testTag("passenger_whatsapp_share_button")
                ) {
                    Icon(
                        Icons.Default.ShareLocation,
                        contentDescription = "Compartir ubicación por WhatsApp",
                        tint = PureWhite
                    )
                }

                Button(
                    onClick = { showSosDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandRed,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("passenger_sos_button")
                ) {
                    Icon(Icons.Default.Emergency, contentDescription = "SOS Policía", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SOS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Floating Window / Draggable Bubble + Floating Controls
        val floatingAccent = when {
            liveTrip != null -> BrandEmerald
            uiState.selectedMode == ServiceMode.DELIVERY -> BrandBlue
            else -> BrandEmerald
        }
        val livePulseTransition = rememberInfiniteTransition(label = "passenger_live_pulse")
        val liveDotAlpha by livePulseTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "passenger_live_dot"
        )

        val centerPassengerLocationAction: () -> Unit = {
            recenterTrigger++
            if (hasLocationPermission) {
                mapViewModel.resetPickupToRealTimeGps()
                fetchLocation()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        AnimatedContent(
            targetState = isFloatingMinimized,
            label = "passenger_floating_window_state",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) { minimized ->
            if (minimized) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = centerPassengerLocationAction,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate900.copy(alpha = 0.92f))
                                .testTag("passenger_my_location_fab")
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Usar mi ubicación en tiempo real",
                                tint = BrandEmerald
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Slate900.copy(alpha = 0.96f),
                            shadowElevation = 16.dp,
                            border = BorderStroke(1.5.dp, floatingAccent.copy(alpha = 0.85f)),
                            modifier = Modifier
                                .widthIn(max = 264.dp)
                                .offset {
                                    IntOffset(
                                        x = floatBubbleOffsetX.roundToInt(),
                                        y = floatBubbleOffsetY.roundToInt()
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        floatBubbleOffsetX = (floatBubbleOffsetX + dragAmount.x).coerceIn(-120f, 12f)
                                        floatBubbleOffsetY = (floatBubbleOffsetY + dragAmount.y).coerceIn(-480f, 0f)
                                    }
                                }
                                .clickable {
                                    floatBubbleOffsetX = 0f
                                    floatBubbleOffsetY = 0f
                                    isFloatingMinimized = false
                                }
                                .testTag("passenger_floating_bubble")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(floatingAccent.copy(alpha = liveDotAlpha))
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Icon(
                                    imageVector = when (uiState.selectedMode) {
                                        ServiceMode.DELIVERY -> Icons.Default.TwoWheeler
                                        ServiceMode.INTERCITY -> Icons.Default.Route
                                        ServiceMode.STANDARD -> Icons.Default.DirectionsCar
                                    },
                                    contentDescription = null,
                                    tint = floatingAccent,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = when {
                                            liveTrip != null -> "${liveTrip.driverName} · ${liveTrip.etaMinutes} min"
                                            uiState.destinationText.isNotBlank() -> uiState.destinationText
                                            else -> "Solicitar ${uiState.selectedMode.label}"
                                        },
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = liveTrip?.phase?.label ?: "Toca para abrir ventana",
                                        color = BrandSkyTint,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.OpenInFull,
                                    contentDescription = "Expandir ventana flotante",
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (liveTrip != null && !uiState.isLiveTripPanelExpanded) {
                            Button(
                                onClick = { mapViewModel.setLiveTripPanelExpanded(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandBlue,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("reopen_live_trip_button")
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ver viaje en tiempo real", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        IconButton(
                            onClick = centerPassengerLocationAction,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate900.copy(alpha = 0.92f))
                                .testTag("passenger_my_location_fab")
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Usar mi ubicación en tiempo real",
                                tint = BrandEmerald
                            )
                        }
                    }

                    // Elevated Floating Window Card over the map (draggable vertically + PiP + minimize)
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = Slate900.copy(alpha = 0.96f),
                        shadowElevation = 18.dp,
                        border = BorderStroke(1.2.dp, floatingAccent.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(0, floatCardOffsetY.roundToInt()) }
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("passenger_floating_window")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 468.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 4.dp)
                                    .width(38.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Slate700)
                            )

                            // Drag handle + PiP + Minimize controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            floatCardOffsetY = (floatCardOffsetY + dragAmount.y)
                                                .coerceIn(-240f, 30f)
                                        }
                                    }
                                    .padding(bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(floatingAccent.copy(alpha = liveDotAlpha))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (liveTrip != null) {
                                        "Viaje activo · ${liveTrip.mode.label}"
                                    } else {
                                        "Ventana flotante · ${uiState.selectedMode.label}"
                                    },
                                    color = floatingAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        if (!onEnterFloatingPip()) {
                                            isFloatingMinimized = true
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("passenger_pip_mode_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPictureAlt,
                                        contentDescription = "Pantalla flotante PiP",
                                        tint = Slate300,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        floatBubbleOffsetX = 0f
                                        floatBubbleOffsetY = 0f
                                        isFloatingMinimized = true
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("passenger_minimize_floating_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Minimizar a burbuja flotante",
                                        tint = Slate300,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (liveTrip != null && uiState.isLiveTripPanelExpanded) {
                                LiveTripTrackingCard(
                                    liveTrip = liveTrip,
                                    gpsStatusLabel = uiState.lastGpsUpdatedLabel,
                                    currentLat = uiState.userLatLng.latitude,
                                    currentLng = uiState.userLatLng.longitude,
                                    onShareWhatsApp = {
                                        shareRouteViaWhatsApp(
                                            context = context,
                                            trustedPhone = trustedContactPhone,
                                            message = mapViewModel.buildWhatsAppShareMessage()
                                        )
                                    },
                                    onOpenSos = { showSosDialog = true },
                                    onMinimize = { mapViewModel.setLiveTripPanelExpanded(false) },
                                    onCloseTrip = mapViewModel::closeLiveTrip
                                )
                            } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ServiceModeChip(
                            icon = Icons.Default.DirectionsCar,
                            label = "Estándar",
                            selected = uiState.selectedMode == ServiceMode.STANDARD,
                            testTag = "mode_standard_chip",
                            onClick = { mapViewModel.setServiceMode(ServiceMode.STANDARD) }
                        )
                        ServiceModeChip(
                            icon = Icons.Default.Route,
                            label = "Ciudad a Ciudad",
                            selected = uiState.selectedMode == ServiceMode.INTERCITY,
                            testTag = "mode_intercity_chip",
                            onClick = { mapViewModel.setServiceMode(ServiceMode.INTERCITY) }
                        )
                        ServiceModeChip(
                            icon = Icons.Default.TwoWheeler,
                            label = "Entregas / Envíos",
                            selected = uiState.selectedMode == ServiceMode.DELIVERY,
                            testTag = "mode_delivery_chip",
                            onClick = { mapViewModel.setServiceMode(ServiceMode.DELIVERY) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        mapViewModel.quickLandmarks.forEach { landmark ->
                            FilterChip(
                                selected = uiState.destinationText == landmark.query,
                                onClick = { mapViewModel.selectQuickLandmark(landmark) },
                                label = { Text(landmark.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Slate800,
                                    labelColor = Slate300,
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = if (uiState.hasGpsFix) BrandEmerald else Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = uiState.lastGpsUpdatedLabel,
                                color = if (uiState.hasGpsFix) BrandEmerald else Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (!uiState.isUsingRealTimeLocation) {
                            Text(
                                text = "Usar ubicación en vivo",
                                color = BrandBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        mapViewModel.resetPickupToRealTimeGps()
                                        fetchLocation()
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (uiState.assistanceNote.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandBlue.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = BrandSkyTint,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.assistanceNote,
                                    color = BrandSkyTint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    val pickupHint = when (uiState.selectedMode) {
                        ServiceMode.DELIVERY -> "Recolección (Ubicación actual en tiempo real)"
                        ServiceMode.INTERCITY -> "Origen (Ubicación actual en tiempo real)"
                        ServiceMode.STANDARD -> "Recogida (Ubicación actual en tiempo real)"
                    }
                    val destHint = when (uiState.selectedMode) {
                        ServiceMode.DELIVERY -> "Entrega de Paquete (¿A dónde lo llevan?)"
                        ServiceMode.INTERCITY -> "Ciudad de Destino (ej. Tulum, Chetumal)"
                        ServiceMode.STANDARD -> "Destino (o toca en el mapa)"
                    }

                    OutlinedTextField(
                        value = uiState.pickupText,
                        onValueChange = mapViewModel::updatePickup,
                        label = { Text(pickupHint, fontSize = 12.sp) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (hasLocationPermission) {
                                        mapViewModel.resetPickupToRealTimeGps()
                                        fetchLocation()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "Ubicación actual en tiempo real",
                                    tint = if (uiState.isUsingRealTimeLocation) BrandEmerald else Slate400
                                )
                            }
                        },
                        singleLine = true,
                        colors = darkMapFieldColors(BrandEmerald),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("pickup_location_input")
                    )

                    OutlinedTextField(
                        value = uiState.destinationText,
                        onValueChange = mapViewModel::updateDestination,
                        label = { Text(destHint, fontSize = 12.sp) },
                        singleLine = true,
                        colors = darkMapFieldColors(BrandRed),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("destination_location_input")
                    )

                    if (uiState.selectedMode == ServiceMode.INTERCITY) {
                        OutlinedTextField(
                            value = uiState.seatsText,
                            onValueChange = mapViewModel::updateSeats,
                            label = { Text("👥 Número de pasajeros / asientos (ej. 2)", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = darkMapFieldColors(BrandBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    if (uiState.selectedMode == ServiceMode.DELIVERY) {
                        OutlinedTextField(
                            value = uiState.packageDetailsText,
                            onValueChange = mapViewModel::updatePackageDetails,
                            label = { Text("📦 ¿Qué envías? (ej. Documentos, Comida)", fontSize = 12.sp) },
                            singleLine = true,
                            colors = darkMapFieldColors(BrandBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.recipientText,
                            onValueChange = mapViewModel::updateRecipient,
                            label = { Text("📞 Nombre y teléfono de quien recibe", fontSize = 12.sp) },
                            singleLine = true,
                            colors = darkMapFieldColors(BrandBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    uiState.suggestedFareMxn?.let { suggested ->
                        val dist = uiState.estimatedDistanceKm ?: 0.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${"%.1f".format(dist)} km · Tarifa sugerida: $$suggested MXN",
                                color = BrandSkyTint,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Usar $$suggested",
                                color = BrandEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { mapViewModel.updateFareOffer(suggested.toString()) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val fareLabel = when (uiState.selectedMode) {
                        ServiceMode.STANDARD -> "Tu oferta de tarifa en $ MXN (ej. $25)"
                        ServiceMode.DELIVERY -> "Tu oferta de tarifa en $ MXN (ej. $30)"
                        ServiceMode.INTERCITY -> "Tu oferta de tarifa en $ MXN (ej. $120)"
                    }

                    OutlinedTextField(
                        value = uiState.fareOfferText,
                        onValueChange = mapViewModel::updateFareOffer,
                        label = { Text(fareLabel, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = BrandEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = darkMapFieldColors(BrandBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("fare_offer_input")
                    )

                    val buttonLabel = when (uiState.selectedMode) {
                        ServiceMode.DELIVERY -> "Solicitar Envío de Paquete"
                        ServiceMode.INTERCITY -> "Solicitar Viaje Interurbano"
                        ServiceMode.STANDARD -> "Solicitar Viaje (Felipe Carrillo Puerto)"
                    }

                    Button(
                        onClick = {
                            val fareNum = uiState.fareOfferText.trim().toIntOrNull()
                            when {
                                uiState.pickupText.isBlank() -> Toast.makeText(
                                    context,
                                    "Indica el punto de recogida",
                                    Toast.LENGTH_SHORT
                                ).show()
                                uiState.destinationText.isBlank() -> Toast.makeText(
                                    context,
                                    "Indica el destino o punto de entrega",
                                    Toast.LENGTH_SHORT
                                ).show()
                                fareNum == null || fareNum < 25 -> Toast.makeText(
                                    context,
                                    "Ingresa una oferta válida (mínimo $25 MXN)",
                                    Toast.LENGTH_SHORT
                                ).show()
                                else -> showConfirmDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandEmerald,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("request_trip_button")
                    ) {
                        Text(buttonLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
private fun PassengerPipFloatingView(
    liveTrip: ActiveLiveTrip?,
    selectedMode: ServiceMode,
    destinationText: String,
    fareOfferText: String
) {
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
                        .background(BrandEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = liveTrip?.phase?.label ?: "ViaKaab ${selectedMode.label}",
                    color = BrandEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Column {
                if (liveTrip != null) {
                    Text(
                        text = "${liveTrip.driverName} · ${liveTrip.etaMinutes} min",
                        color = BrandSkyTint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = liveTrip.destination,
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = destinationText.ifBlank { "Selecciona destino en mapa" },
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val fareDisplay = liveTrip?.fareMxn?.let { "$$it MXN" }
                ?: fareOfferText.takeIf { it.isNotBlank() }?.let { "$$it MXN" }
                ?: "Felipe Carrillo Puerto"
            Text(
                text = fareDisplay,
                color = Slate300,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ServiceModeChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) BrandBlue else Slate800,
            contentColor = if (selected) PureWhite else Slate300
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .height(44.dp)
            .testTag(testTag)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
private fun darkMapFieldColors(strokeColor: Color) =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Slate800,
        unfocusedContainerColor = Slate800,
        focusedTextColor = PureWhite,
        unfocusedTextColor = PureWhite,
        focusedBorderColor = strokeColor,
        unfocusedBorderColor = Slate700,
        focusedLabelColor = Slate300,
        unfocusedLabelColor = Slate400,
        cursorColor = PureWhite
    )
