package com.example.viakaab.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.ui.components.ViaKaabInteractiveMap
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.viewmodel.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap

@SuppressLint("MissingPermission")
@Composable
fun PassengerMapScreen(
    mapViewModel: MapViewModel,
    initialMode: ServiceMode = ServiceMode.STANDARD,
    initialAssistanceNote: String = "",
    onBack: () -> Unit,
    onOpenProfileMenu: () -> Unit
) {
    val context = LocalContext.current
    val uiState by mapViewModel.passengerState.collectAsStateWithLifecycle()
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

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(initialMode, initialAssistanceNote) {
        mapViewModel.applyEntryContext(initialMode, initialAssistanceNote)
    }

    fun fetchLocation() {
        if (!hasLocationPermission) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                mapViewModel.onUserLocationResolved(location.latitude, location.longitude)
            } else {
                mapViewModel.onUserLocationResolved(19.5775, -88.0453)
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

    if (showConfirmDialog) {
        val title = when (uiState.selectedMode) {
            ServiceMode.DELIVERY -> "Solicitud de Envío de Paquete"
            ServiceMode.INTERCITY -> "Solicitud de Viaje Interurbano"
            ServiceMode.STANDARD -> "Solicitud de Viaje Local"
        }
        val details = buildString {
            append("🟢 Origen / Recogida:\n${uiState.pickupText}\n\n")
            append("🔴 Destino / Entrega:\n${uiState.destinationText}\n\n")
            uiState.estimatedDistanceKm?.let { dist ->
                append("📏 Distancia estimada: ${"%.1f".format(dist)} km\n\n")
            }
            if (uiState.assistanceNote.isNotBlank()) {
                append("ℹ️ Nota: ${uiState.assistanceNote}\n\n")
            }
            if (uiState.selectedMode == ServiceMode.INTERCITY) {
                val seats = uiState.seatsText.ifBlank { "1" }
                append("👥 Pasajeros/Asientos: $seats\n\n")
            } else if (uiState.selectedMode == ServiceMode.DELIVERY) {
                if (uiState.packageDetailsText.isNotBlank()) {
                    append("📦 Paquete: ${uiState.packageDetailsText}\n")
                }
                if (uiState.recipientText.isNotBlank()) {
                    append("📞 Recibe: ${uiState.recipientText}\n\n")
                }
            }
            append("💵 Tu oferta de tarifa:\n$${uiState.fareOfferText} MXN\n\n")
            append("¿Deseas enviar esta oferta a los conductores y repartidores?")
        }

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(title) },
            text = { Text(details) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    mapViewModel.publishTripOffer()
                }) {
                    Text("Publicar Oferta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        ViaKaabInteractiveMap(
            pickupLatLng = uiState.userLatLng,
            pickupTitle = "Tu ubicación",
            pickupSnippet = "Punto de recogida GPS",
            destinationLatLng = uiState.destinationLatLng,
            destinationTitle = uiState.destinationMarkerTitle.ifBlank { uiState.destinationText },
            destinationSnippet = "Punto de llegada de viaje / entrega",
            routeColor = BrandBlue,
            hasLocationPermission = hasLocationPermission,
            landmarks = mapViewModel.quickLandmarks,
            bottomOverlayReserveDp = 360.dp,
            badgeTopPaddingDp = 18.dp,
            onMapTap = mapViewModel::onMapTapped,
            onGoogleMapReady = { googleMapRef = it },
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar: Back + Menu
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900.copy(alpha = 0.85f))
                    .testTag("passenger_map_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = PureWhite)
            }
            IconButton(
                onClick = onOpenProfileMenu,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900.copy(alpha = 0.85f))
                    .testTag("passenger_map_menu_button")
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = PureWhite)
            }
        }

        // Bottom Sheet + My Location FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp, bottom = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(
                    onClick = {
                        if (hasLocationPermission) {
                            fetchLocation()
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate900.copy(alpha = 0.88f))
                        .testTag("passenger_my_location_fab")
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = PureWhite
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Slate900)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceModeChip(
                        label = "🚗 Estándar",
                        selected = uiState.selectedMode == ServiceMode.STANDARD,
                        testTag = "mode_standard_chip",
                        onClick = { mapViewModel.setServiceMode(ServiceMode.STANDARD) }
                    )
                    ServiceModeChip(
                        label = "🚕 Ciudad a Ciudad",
                        selected = uiState.selectedMode == ServiceMode.INTERCITY,
                        testTag = "mode_intercity_chip",
                        onClick = { mapViewModel.setServiceMode(ServiceMode.INTERCITY) }
                    )
                    ServiceModeChip(
                        label = "🛵 Entregas / Envíos",
                        selected = uiState.selectedMode == ServiceMode.DELIVERY,
                        testTag = "mode_delivery_chip",
                        onClick = { mapViewModel.setServiceMode(ServiceMode.DELIVERY) }
                    )
                }

                // Quick Felipe Carrillo Puerto landmark chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 10.dp),
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

                if (uiState.assistanceNote.isNotBlank()) {
                    Text(
                        text = "ℹ️ ${uiState.assistanceNote}",
                        color = BrandSkyTint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                val pickupHint = when (uiState.selectedMode) {
                    ServiceMode.DELIVERY -> "🟢 Recolección de Paquete (¿Dónde lo recogen?)"
                    ServiceMode.INTERCITY -> "🟢 Ciudad de Origen (ej. Felipe Carrillo Puerto)"
                    ServiceMode.STANDARD -> "🟢 Punto de Recogida (¿Dónde te recogemos?)"
                }
                val destHint = when (uiState.selectedMode) {
                    ServiceMode.DELIVERY -> "🔴 Entrega de Paquete (¿A dónde lo llevan?)"
                    ServiceMode.INTERCITY -> "🔴 Ciudad de Destino (ej. Tulum, Chetumal)"
                    ServiceMode.STANDARD -> "🔴 Destino (o toca en el mapa)"
                }

                OutlinedTextField(
                    value = uiState.pickupText,
                    onValueChange = mapViewModel::updatePickup,
                    label = { Text(pickupHint, fontSize = 12.sp) },
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
                            text = "📏 ${"%.1f".format(dist)} km · Sugerido: $$suggested MXN",
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

                OutlinedTextField(
                    value = uiState.fareOfferText,
                    onValueChange = mapViewModel::updateFareOffer,
                    label = { Text("💵 Tu oferta de tarifa en $ MXN (ej. 40)", fontSize = 12.sp) },
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
                                "Indica el punto de origen o recolección",
                                Toast.LENGTH_SHORT
                            ).show()
                            uiState.destinationText.isBlank() -> Toast.makeText(
                                context,
                                "Indica el destino o punto de entrega",
                                Toast.LENGTH_SHORT
                            ).show()
                            fareNum == null || fareNum < 20 -> Toast.makeText(
                                context,
                                "Ingresa una oferta válida (mínimo $20 MXN)",
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

@Composable
private fun ServiceModeChip(
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
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .height(44.dp)
            .testTag(testTag)
    ) {
        Text(text = label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
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
        unfocusedBorderColor = Slate800,
        focusedLabelColor = Slate400,
        unfocusedLabelColor = Slate400,
        cursorColor = PureWhite
    )
