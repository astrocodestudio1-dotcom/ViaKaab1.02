package com.example.viakaab.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viakaab.model.ActiveLiveTrip
import com.example.viakaab.model.LiveTripPhase
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate600
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900

@Composable
fun LiveTripTrackingCard(
    liveTrip: ActiveLiveTrip,
    gpsStatusLabel: String,
    currentLat: Double,
    currentLng: Double,
    onShareWhatsApp: () -> Unit,
    onOpenSos: () -> Unit,
    onMinimize: () -> Unit,
    onCloseTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = when (liveTrip.phase) {
        LiveTripPhase.SEARCHING -> 0.25f
        LiveTripPhase.DRIVER_ARRIVING -> 0.55f
        LiveTripPhase.IN_PROGRESS -> 0.85f
        LiveTripPhase.COMPLETED -> 1.0f
    }

    val pulseTransition = rememberInfiniteTransition(label = "live_trip_pulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_trip_dot_alpha"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(BrandEmerald.copy(alpha = dotAlpha))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Viaje en tiempo real",
                color = PureWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onMinimize) {
                Text("Ver mapa / editar", color = BrandSkyTint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandBlue.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = liveTrip.phase.label,
                        color = BrandEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandEmerald.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "$${liveTrip.fareMxn} MXN",
                            color = BrandEmerald,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    color = BrandEmerald,
                    trackColor = Slate700,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PhaseStepLabel(
                        text = "Buscando",
                        active = liveTrip.phase.ordinal >= LiveTripPhase.SEARCHING.ordinal
                    )
                    PhaseStepLabel(
                        text = "En camino",
                        active = liveTrip.phase.ordinal >= LiveTripPhase.DRIVER_ARRIVING.ordinal
                    )
                    PhaseStepLabel(
                        text = "En curso",
                        active = liveTrip.phase.ordinal >= LiveTripPhase.IN_PROGRESS.ordinal
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = BrandEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = liveTrip.origin,
                        color = Slate300,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = BrandRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = liveTrip.destination,
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate900.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${liveTrip.driverName} · ${liveTrip.vehicleSummary}",
                            color = BrandSkyTint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (liveTrip.etaMinutes > 0) "~${liveTrip.etaMinutes} min" else "Llegaste",
                            color = BrandEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$gpsStatusLabel (${"%.4f".format(currentLat)}, ${"%.4f".format(currentLng)})",
                    color = Slate400,
                    fontSize = 11.sp
                )

                if (liveTrip.mode == ServiceMode.DELIVERY && liveTrip.deliveryCode.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandEmerald.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_delivery_pin_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Código de entrega",
                                tint = BrandEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tu código de entrega: ${liveTrip.deliveryCode}",
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Muéstraselo o díselo al repartidor solo al recibir tu pedido.",
                                    color = BrandSkyTint,
                                    fontSize = 11.sp
                                )
                                if (liveTrip.pickupCode.isNotBlank()) {
                                    Text(
                                        text = "Código comercio: ${liveTrip.pickupCode}",
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onShareWhatsApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandEmerald,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("live_trip_whatsapp_button")
            ) {
                Icon(Icons.Default.ShareLocation, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = onOpenSos,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandRed,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("live_trip_sos_button")
            ) {
                Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SOS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        OutlinedButton(
            onClick = onCloseTrip,
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("live_trip_close_button")
        ) {
            Text(
                text = if (liveTrip.phase == LiveTripPhase.COMPLETED) {
                    "Finalizar y pedir otro viaje"
                } else {
                    "Cancelar seguimiento / Nuevo viaje"
                },
                color = Slate300,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PhaseStepLabel(text: String, active: Boolean) {
    Text(
        text = text,
        color = if (active) BrandEmerald else Slate600,
        fontSize = 10.sp,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
    )
}
