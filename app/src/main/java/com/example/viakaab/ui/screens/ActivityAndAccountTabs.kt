package com.example.viakaab.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.viakaab.model.InboxMessage
import com.example.viakaab.model.ScheduledRide
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.model.TripRecord
import com.example.viakaab.model.UserRole
import com.example.viakaab.model.UserSession
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900

@Composable
fun ActivityTabContent(
    trips: List<TripRecord>,
    scheduledRides: List<ScheduledRide>,
    onRepeatTrip: (ServiceMode) -> Unit,
    onCancelScheduled: (String) -> Unit,
    onRequestTrip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .background(Slate900)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Actividad",
            color = PureWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (scheduledRides.isNotEmpty()) {
            Text(
                text = "Reservas programadas",
                color = BrandSkyTint,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
            scheduledRides.forEach { ride ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📅 ${ride.destination}",
                                color = PureWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${ride.dateLabel} · ${ride.timeLabel} · ${ride.mode.label}",
                                color = Slate400,
                                fontSize = 13.sp
                            )
                        }
                        TextButton(onClick = { onCancelScheduled(ride.id) }) {
                            Text("Cancelar", color = BrandRed)
                        }
                    }
                }
            }
        }

        Text(
            text = "Recientes",
            color = PureWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (trips.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Aún no tienes viajes recientes", color = Slate400)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onRequestTrip,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSky, contentColor = PureWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Solicitar mi primer viaje", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            trips.forEach { trip ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onRepeatTrip(trip.mode) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚗 ${trip.origin} ➔ ${trip.destination}",
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = trip.fareMxn,
                                color = BrandEmerald,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${trip.timestampLabel} · ${trip.statusLabel}",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountTabContent(
    session: UserSession,
    inboxMessages: List<InboxMessage>,
    onMarkInboxRead: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenGeminiChat: () -> Unit,
    onOpenDriverProfile: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }
    var showInboxDialog by rememberSaveable { mutableStateOf(false) }
    var showIncentivesDialog by rememberSaveable { mutableStateOf(false) }
    var showRoleSwitcherDialog by rememberSaveable { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas salir?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) {
                    Text("Sí, salir", color = BrandRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showInboxDialog) {
        AlertDialog(
            onDismissRequest = { showInboxDialog = false },
            title = { Text("Buzón de Mensajes") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    inboxMessages.forEach { msg ->
                        Column {
                            Text("${msg.title} (${msg.timeLabel})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(msg.body, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInboxDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showIncentivesDialog) {
        AlertDialog(
            onDismissRequest = { showIncentivesDialog = false },
            title = { Text("Incentivos ViaKaab") },
            text = {
                Text(
                    "• Tarifa Justa Directa: Sin comisiones sorpresa en Felipe Carrillo Puerto.\n" +
                        "• Prioridad Local: Los conductores cercanos ven tu oferta al instante.\n" +
                        "• Asistencia 24/7 incluida en todos tus viajes y envíos."
                )
            },
            confirmButton = {
                TextButton(onClick = { showIncentivesDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showRoleSwitcherDialog) {
        AlertDialog(
            onDismissRequest = { showRoleSwitcherDialog = false },
            title = { Text("Cambiar modo de uso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { role ->
                        OutlinedButton(
                            onClick = {
                                showRoleSwitcherDialog = false
                                onSwitchRole(role)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val marker = if (role == session.role) " ✓" else ""
                            Text("${role.displayName}$marker")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleSwitcherDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .background(Slate900)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenProfile)
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .padding(bottom = 16.dp)
                .testTag("account_profile_header"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.effectiveDisplayName,
                    color = PureWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "${session.role.displayName} · ★ 5.0 · Ver perfil",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Slate700),
                contentAlignment = Alignment.Center
            ) {
                if (session.profileImageUri.isNotBlank()) {
                    AsyncImage(
                        model = Uri.parse(session.profileImageUri),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = session.initials,
                        color = PureWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountTileCard(
                emoji = "🛟",
                label = "Ayuda",
                modifier = Modifier.weight(1f),
                testTag = "account_help_card",
                onClick = onOpenSupport
            )
            AccountTileCard(
                emoji = "👛",
                label = "Billetera",
                modifier = Modifier.weight(1f),
                testTag = "account_wallet_card",
                onClick = onOpenPaymentMethods
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountTileCard(
                emoji = "🛡️",
                label = "Seguridad",
                modifier = Modifier.weight(1f),
                testTag = "account_safety_card",
                onClick = onOpenTerms
            )
            AccountTileCard(
                emoji = "✉️",
                label = "Buzón",
                modifier = Modifier.weight(1f),
                testTag = "account_inbox_card",
                onClick = {
                    onMarkInboxRead()
                    showInboxDialog = true
                }
            )
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable(onClick = onOpenGeminiChat)
                .testTag("account_gemini_ai_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🤖 Asistente IA ViaKaab",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Consulta tarifas sugeridas, rutas y consejos en Felipe Carrillo Puerto",
                    color = BrandSkyTint,
                    fontSize = 14.sp
                )
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { showIncentivesDialog = true }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tienes varios incentivos",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Toca para ver beneficios y descuentos activos en tu zona",
                    color = Slate400,
                    fontSize = 14.sp
                )
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { showRoleSwitcherDialog = true }
                .testTag("account_switch_role_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🔄 Cambiar rol (Modo actual: ${session.role.displayName})",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Cambia entre Pasajero, Conductor o Repartidor en cualquier momento",
                    color = Slate400,
                    fontSize = 13.sp
                )
            }
        }

        if (session.role.isDriverRole) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSky),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable(onClick = onOpenDriverProfile)
                    .testTag("account_driver_panel_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🚗 Panel de Conductor / Repartidor",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Gestiona tu vehículo, placas, licencia e INE",
                        color = BrandSkyTint,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Text(
            text = "Cerrar sesión",
            color = BrandRed,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLogoutConfirm = true }
                .padding(vertical = 14.dp)
                .testTag("account_logout_button")
        )
    }
}

@Composable
private fun AccountTileCard(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
