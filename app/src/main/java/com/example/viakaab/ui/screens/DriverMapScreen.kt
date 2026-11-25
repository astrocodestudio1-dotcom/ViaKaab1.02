package com.example.viakaab.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.model.DriverTripStage
import com.example.viakaab.model.UserRole
import com.example.viakaab.ui.components.ViaKaabInteractiveMap
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

@SuppressLint("MissingPermission")
@Composable
fun DriverMapScreen(
    mapViewModel: MapViewModel,
    onOpenDriverProfile: () -> Unit
) {
    val context = LocalContext.current
    val session by mapViewModel.session.collectAsStateWithLifecycle()
    val driverState by mapViewModel.driverState.collectAsStateWithLifecycle()
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            runCatching { googleMapRef?.isMyLocationEnabled = true }
        }
    }

    var showAcceptDialog by rememberSaveable { mutableStateOf(false) }
    var showCounterDialog by rememberSaveable { mutableStateOf(false) }
    var counterAmount by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(driverState.toastMessage) {
        driverState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mapViewModel.clearDriverToast()
        }
    }

    val req = driverState.activeRequest

    val headerText = when (session.role) {
        UserRole.DELIVERY -> "Solicitudes de entregas en Felipe Carrillo Puerto"
        UserRole.BOTH -> "Viajes y entregas en Felipe Carrillo Puerto"
        else -> "Solicitudes cercanas en Felipe Carrillo Puerto"
    }

    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("¡Viaje Aceptado!") },
            text = {
                Text(
                    "Has aceptado: ${req.origin} ➔ ${req.destination} por $${req.fareMxn} MXN.\n" +
                        "Dirígete al punto de salida del pasajero."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showAcceptDialog = false
                    mapViewModel.startDriverRoute()
                }) {
                    Text("Iniciar Ruta")
                }
            }
        )
    }

    if (showCounterDialog) {
        AlertDialog(
            onDismissRequest = { showCounterDialog = false },
            title = { Text("Contraofertar Tarifa") },
            text = {
                Column {
                    Text("Ingresa tu contraoferta en $ MXN para el pasajero:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = counterAmount,
                        onValueChange = { counterAmount = it },
                        label = { Text("Monto oferta (ej. 50)") },
                        singleLine = true,
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
                    Text("Enviar Oferta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCounterDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ViaKaabInteractiveMap(
            pickupLatLng = LatLng(req.startLat, req.startLng),
            pickupTitle = "Salida: ${req.origin}",
            pickupSnippet = "${req.passengerName} ($${req.fareMxn} MXN)",
            destinationLatLng = LatLng(req.endLat, req.endLng),
            destinationTitle = "Destino: ${req.destination}",
            destinationSnippet = req.serviceTypeLabel,
            routeColor = BrandEmerald,
            hasLocationPermission = hasLocationPermission,
            landmarks = mapViewModel.quickLandmarks,
            bottomOverlayReserveDp = 240.dp,
            badgeTopPaddingDp = 78.dp,
            onGoogleMapReady = { googleMapRef = it },
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar: Menu, Earnings Pill & Online Switch
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenDriverProfile,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900.copy(alpha = 0.88f))
                    .testTag("driver_menu_button")
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menú Conductor", tint = PureWhite)
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900.copy(alpha = 0.88f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$${driverState.earningsTodayMxn} MXN",
                    color = BrandEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${driverState.completedTripsToday} viajes hoy",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate900.copy(alpha = 0.88f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (driverState.isOnline) "En línea" else "Offline",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = driverState.isOnline,
                    onCheckedChange = mapViewModel::toggleDriverOnline,
                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = BrandEmerald),
                    modifier = Modifier.testTag("driver_online_switch")
                )
            }
        }

        // Bottom Sheet + MyLocation FAB
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
                            val map = googleMapRef
                            if (map != null) {
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(19.5775, -88.0453), 15f)
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Mapa centrado en Felipe Carrillo Puerto",
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Slate900)
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (!driverState.isOnline) "Modo fuera de servicio" else headerText,
                    color = PureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (!driverState.isOnline) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Activa el interruptor 'En línea' arriba para recibir viajes y entregas cerca de ti.",
                                color = Slate400,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍 ${req.origin} ➔ ${req.destination}",
                                    color = PureWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$${req.fareMxn} MXN",
                                    color = BrandEmerald,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${req.serviceTypeLabel} · ${req.passengerName} · ${req.distanceKm}",
                                color = BrandSkyTint,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

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
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("driver_accept_trip_button")
                                        ) {
                                            Text("Aceptar ($${req.fareMxn})", fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { showCounterDialog = true },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("driver_counter_offer_button")
                                        ) {
                                            Text("Ofertar", color = PureWhite)
                                        }

                                        OutlinedButton(
                                            onClick = mapViewModel::skipDriverRequest,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text("Omitir", color = Slate400, fontSize = 12.sp)
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
                                        Text("Pasajero a Bordo · Iniciar Viaje", fontWeight = FontWeight.Bold)
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
                                            "Finalizar Viaje y Cobrar $${req.fareMxn} MXN",
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
