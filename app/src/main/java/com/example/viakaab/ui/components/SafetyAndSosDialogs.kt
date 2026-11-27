package com.example.viakaab.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viakaab.data.SessionRepository
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
import com.example.viakaab.ui.theme.Slate950

fun dialPhoneNumber(context: Context, phone: String) {
    val sanitized = phone.filter { it.isDigit() || it == '+' }
    if (sanitized.isBlank()) {
        Toast.makeText(context, "Configura un número telefónico válido", Toast.LENGTH_SHORT).show()
        return
    }
    runCatching {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized")))
    }.onFailure {
        Toast.makeText(context, "No se pudo abrir la app de teléfono", Toast.LENGTH_SHORT).show()
    }
}

fun shareRouteViaWhatsApp(
    context: Context,
    trustedPhone: String,
    message: String
) {
    val digits = trustedPhone.filter { it.isDigit() }
    if (digits.length >= 10) {
        val waDigits = when {
            digits.length == 10 -> "52$digits"
            else -> digits
        }
        val waUri = Uri.parse("https://wa.me/$waDigits?text=${Uri.encode(message)}")
        val opened = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, waUri))
        }.isSuccess
        if (opened) return
    }

    val directWa = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        setPackage("com.whatsapp")
        putExtra(Intent.EXTRA_TEXT, message)
    }
    val openedDirect = runCatching { context.startActivity(directWa) }.isSuccess
    if (!openedDirect) {
        val fallback = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            },
            "Enviar recorrido y ubicación en tiempo real"
        )
        runCatching { context.startActivity(fallback) }.onFailure {
            Toast.makeText(context, "No se encontró una app para compartir", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun SafetyAndSosDialog(
    supportPhone: String,
    trustedContactPhone: String,
    shareMessage: String,
    onSaveSupportPhone: (String) -> Unit,
    onSaveTrustedContactPhone: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var editingSupport by rememberSaveable { mutableStateOf(false) }
    var supportInput by rememberSaveable(supportPhone) { mutableStateOf(supportPhone) }
    var trustedInput by rememberSaveable(trustedContactPhone) { mutableStateOf(trustedContactPhone) }

    val darkFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Slate950,
        unfocusedContainerColor = Slate950,
        focusedTextColor = PureWhite,
        unfocusedTextColor = PureWhite,
        focusedBorderColor = BrandEmerald,
        unfocusedBorderColor = Slate700,
        focusedLabelColor = BrandEmerald,
        unfocusedLabelColor = Slate400,
        cursorColor = BrandEmerald
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        titleContentColor = PureWhite,
        textContentColor = Slate300,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = BrandRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Seguridad y Emergencias",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Policía Municipal Felipe Carrillo Puerto + 911
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = BorderStroke(1.dp, BrandRed.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Policía Municipal · Felipe Carrillo Puerto",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Seguridad Pública: ${SessionRepository.FCP_POLICE_DISPLAY} · Nacional: 911",
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    dialPhoneNumber(context, SessionRepository.FCP_POLICE_PHONE)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandRed,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("sos_call_police_fcp_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Policía FCP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    dialPhoneNumber(context, SessionRepository.EMERGENCY_911)
                                },
                                border = BorderStroke(1.dp, BrandRed.copy(alpha = 0.65f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("sos_call_911_button")
                            ) {
                                Text("911", color = BrandRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 2. WhatsApp Live Location & Route Share
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Compartir recorrido por WhatsApp",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Envía tu ubicación GPS en tiempo real, origen y destino a un contacto de confianza.",
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = trustedInput,
                            onValueChange = { trustedInput = it },
                            label = { Text("WhatsApp contacto (opcional, 10 dígitos)", fontSize = 12.sp) },
                            singleLine = true,
                            colors = darkFieldColors,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("trusted_contact_phone_input")
                        )
                        Button(
                            onClick = {
                                val cleaned = trustedInput.trim()
                                if (cleaned.isNotBlank()) {
                                    onSaveTrustedContactPhone(cleaned)
                                }
                                shareRouteViaWhatsApp(context, cleaned, shareMessage)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandEmerald,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("share_whatsapp_live_button")
                        ) {
                            Icon(Icons.Default.ShareLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar por WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // 3. Configurable ViaKaab Support Phone
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = BrandSkyTint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Soporte ViaKaab",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { editingSupport = !editingSupport },
                                modifier = Modifier.testTag("toggle_edit_support_phone")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar número",
                                    tint = BrandSkyTint,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (editingSupport) "Listo" else "Cambiar",
                                    color = BrandSkyTint,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (editingSupport) {
                            OutlinedTextField(
                                value = supportInput,
                                onValueChange = { supportInput = it },
                                label = { Text("Número telefónico de soporte ViaKaab", fontSize = 12.sp) },
                                singleLine = true,
                                colors = darkFieldColors,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .testTag("support_phone_edit_input")
                            )
                            Button(
                                onClick = {
                                    val trimmed = supportInput.trim()
                                    if (trimmed.filter { it.isDigit() }.length >= 7) {
                                        onSaveSupportPhone(trimmed)
                                        editingSupport = false
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Ingresa un número telefónico válido",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = PureWhite),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("save_support_phone_button")
                            ) {
                                Text("Guardar número de soporte", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Teléfono actual: $supportPhone",
                                color = Slate300,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedButton(
                                onClick = { dialPhoneNumber(context, supportPhone) },
                                border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.65f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("call_viakaab_support_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = BrandSkyTint, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Llamar a Soporte ViaKaab", color = BrandSkyTint, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = BrandEmerald, fontWeight = FontWeight.Bold)
            }
        }
    )
}
