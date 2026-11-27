package com.example.viakaab.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.R
import com.example.viakaab.data.SessionRepository
import com.example.viakaab.model.PaymentMethodType
import com.example.viakaab.ui.components.dialPhoneNumber
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
import com.example.viakaab.ui.theme.Slate950
import com.example.viakaab.viewmodel.MainAppViewModel

@Composable
private fun darkAccountFieldColors() = OutlinedTextFieldDefaults.colors(
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

@Composable
fun PaymentMethodsScreen(
    viewModel: MainAppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val selectedPayment by viewModel.selectedPayment.collectAsStateWithLifecycle()
    val savedCard by viewModel.savedCard.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    var showCardDialog by rememberSaveable { mutableStateOf(false) }
    var showSpeiDialog by rememberSaveable { mutableStateOf(false) }
    var cardAlias by rememberSaveable { mutableStateOf("Tarjeta Principal") }
    var cardLast4 by rememberSaveable { mutableStateOf("") }
    var cardBrand by rememberSaveable { mutableStateOf("Visa / Mastercard") }

    LaunchedEffect(actionState.message) {
        actionState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    if (showCardDialog) {
        AlertDialog(
            onDismissRequest = { showCardDialog = false },
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Registrar Referencia de Tarjeta", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Para tu seguridad, solo guardamos un alias y los últimos 4 dígitos para identificar tu pago con terminal o transferencia.",
                        fontSize = 13.sp,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cardAlias,
                        onValueChange = { cardAlias = it },
                        label = { Text("Alias (ej. Débito Personal)") },
                        singleLine = true,
                        colors = darkAccountFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardBrand,
                        onValueChange = { cardBrand = it },
                        label = { Text("Red (Visa, Mastercard)") },
                        singleLine = true,
                        colors = darkAccountFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardLast4,
                        onValueChange = { input ->
                            cardLast4 = input.filter { it.isDigit() }.take(4)
                        },
                        label = { Text("Últimos 4 dígitos") },
                        singleLine = true,
                        colors = darkAccountFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (cardLast4.length == 4) {
                        viewModel.saveMaskedCard(
                            alias = cardAlias.trim().ifBlank { "Tarjeta" },
                            last4 = cardLast4,
                            brandLabel = cardBrand.trim().ifBlank { "Tarjeta" }
                        )
                        showCardDialog = false
                    } else {
                        Toast.makeText(context, "Ingresa exactamente 4 dígitos", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Guardar", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCardDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    if (showSpeiDialog) {
        val clabe = "646180110400000001"
        AlertDialog(
            onDismissRequest = { showSpeiDialog = false },
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Transferencia Bancaria SPEI", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Usa estos datos para recargar o pagar al conductor por SPEI:", fontSize = 13.sp, color = Slate300)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Beneficiario: ViaKaab Movilidad Local", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PureWhite)
                    Text("Institución: STP (Sistema de Transferencias)", fontSize = 13.sp, color = Slate400)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("CLABE: $clabe", fontWeight = FontWeight.Bold, color = BrandEmerald, fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(clabe))
                    viewModel.selectPaymentMethod(PaymentMethodType.SPEI)
                    Toast.makeText(context, "CLABE copiada y SPEI seleccionado", Toast.LENGTH_SHORT).show()
                    showSpeiDialog = false
                }) {
                    Text("Copiar CLABE y Activar", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSpeiDialog = false }) {
                    Text("Cerrar", color = Slate400)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate950, Slate900, Slate950)))
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = PureWhite)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Métodos de Pago", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandEmerald.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = BrandEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Método activo: ${selectedPayment.title}", color = BrandSkyTint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$0.00 MXN Crédito ViaKaab", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            PaymentMethodRowCard(
                icon = Icons.Default.Payments,
                iconTint = BrandEmerald,
                title = "Efectivo (Pago directo)",
                badgeText = if (selectedPayment == PaymentMethodType.CASH) "Activo ✓" else "Seleccionar",
                badgeColor = if (selectedPayment == PaymentMethodType.CASH) BrandEmerald else BrandSkyTint,
                isActive = selectedPayment == PaymentMethodType.CASH,
                onClick = { viewModel.selectPaymentMethod(PaymentMethodType.CASH) }
            )

            val cardTitle = savedCard?.let { "${it.brandLabel} •••• ${it.last4}" }
                ?: "Tarjeta de Crédito / Débito"
            PaymentMethodRowCard(
                icon = Icons.Default.CreditCard,
                iconTint = BrandBlue,
                title = cardTitle,
                badgeText = if (selectedPayment == PaymentMethodType.CARD) "Activo ✓" else "+ Configurar",
                badgeColor = if (selectedPayment == PaymentMethodType.CARD) BrandEmerald else BrandSkyTint,
                isActive = selectedPayment == PaymentMethodType.CARD,
                onClick = { showCardDialog = true }
            )

            PaymentMethodRowCard(
                icon = Icons.Default.AccountBalance,
                iconTint = BrandSky,
                title = "Transferencia Bancaria SPEI",
                badgeText = if (selectedPayment == PaymentMethodType.SPEI) "Activo ✓" else "+ Ver CLABE",
                badgeColor = if (selectedPayment == PaymentMethodType.SPEI) BrandEmerald else BrandSkyTint,
                isActive = selectedPayment == PaymentMethodType.SPEI,
                onClick = { showSpeiDialog = true }
            )
        }
    }
}

@Composable
private fun PaymentMethodRowCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    badgeText: String,
    badgeColor: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = BorderStroke(1.dp, if (isActive) BrandEmerald.copy(alpha = 0.65f) else Slate700),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(21.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(text = badgeText, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun SupportScreen(
    viewModel: MainAppViewModel,
    onBack: () -> Unit,
    onOpenTerms: () -> Unit
) {
    val context = LocalContext.current
    val supportPhone by viewModel.supportPhone.collectAsStateWithLifecycle()
    var expandedFaqIndex by rememberSaveable { mutableIntStateOf(-1) }
    var showEditSupportDialog by rememberSaveable { mutableStateOf(false) }
    var supportDraft by rememberSaveable(supportPhone) { mutableStateOf(supportPhone) }

    if (showEditSupportDialog) {
        AlertDialog(
            onDismissRequest = { showEditSupportDialog = false },
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Teléfono de Soporte ViaKaab", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = supportDraft,
                    onValueChange = { supportDraft = it },
                    label = { Text("Número telefónico (ej. +52 983 000 0000)") },
                    singleLine = true,
                    colors = darkAccountFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = supportDraft.trim()
                    if (trimmed.filter { it.isDigit() }.length >= 7) {
                        viewModel.saveSupportPhone(trimmed)
                        showEditSupportDialog = false
                    } else {
                        Toast.makeText(context, "Ingresa un número válido", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Guardar", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSupportDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    val faqs = listOf(
        "¿Cómo cambio mi método de pago?" to
            "Entra a Cuenta ➔ Billetera o Perfil ➔ Métodos de pago y elige entre Efectivo, Tarjeta o Transferencia SPEI.",
        "¿Qué hago si olvidé un objeto en el viaje?" to
            "Contáctanos de inmediato por correo a soporte@viakaab.com o llama al $supportPhone con la hora y ruta de tu viaje.",
        "¿Cómo cambio mi rol a Conductor o Repartidor?" to
            "En Cuenta o Mi Perfil toca 'Cambiar rol', elige Conductor, Repartidor o Ambos y completa tus documentos en el Panel de Conductor."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate950, Slate900, Slate950)))
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = PureWhite)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Soporte y Ayuda", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("¿Cómo podemos ayudarte?", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Nuestro equipo de atención al cliente está disponible 24/7 para resolver cualquier duda sobre viajes, entregas o privacidad.",
                        color = Slate400,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Correo electrónico:", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "soporte@viakaab.com",
                        color = BrandEmerald,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:soporte@viakaab.com"))
                                    )
                                }
                            }
                            .padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Teléfono de soporte ViaKaab:", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = supportPhone,
                                color = BrandSkyTint,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { dialPhoneNumber(context, supportPhone) }
                                    .padding(vertical = 4.dp)
                            )
                        }
                        TextButton(onClick = { showEditSupportDialog = true }) {
                            Text("Cambiar número", color = BrandEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { dialPhoneNumber(context, SessionRepository.FCP_POLICE_PHONE) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = PureWhite),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SOS Policía Carrillo Puerto (${SessionRepository.FCP_POLICE_DISPLAY})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Preguntas Frecuentes", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    faqs.forEachIndexed { index, (question, answer) ->
                        val expanded = expandedFaqIndex == index
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedFaqIndex = if (expanded) -1 else index }
                                .padding(vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = question,
                                    color = PureWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (expanded) "Contraer" else "Expandir",
                                    tint = BrandSkyTint
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Text(
                                    text = answer,
                                    color = Slate300,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onOpenTerms,
                border = BorderStroke(1.dp, Slate700),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("support_view_terms_button")
            ) {
                Text("Consultar Términos y Políticas", color = PureWhite, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate950, Slate900, Slate950)))
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = PureWhite,
                modifier = Modifier
                    .size(width = 148.dp, height = 76.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_viakaab),
                    contentDescription = "Logo ViaKaab",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Términos y Condiciones y Política de Privacidad",
                color = PureWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Text(
                text = "Última actualización: 2026\nViaKaab - Felipe Carrillo Puerto, Quintana Roo, México",
                color = BrandSkyTint,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 22.dp)
            )

            TermsSection(
                title = "1. Términos del Servicio",
                body = "ViaKaab es una plataforma tecnológica local que conecta a pasajeros, conductores y repartidores en Felipe Carrillo Puerto. Al utilizar la aplicación, aceptas cumplir con las reglas de convivencia, respeto mutuo y seguridad durante la prestación o solicitud de los servicios."
            )
            TermsSection(
                title = "2. Política de Privacidad y Datos Personales",
                body = "Tus datos personales (nombre, teléfono, correo electrónico y ubicación GPS durante el uso de mapas) son recopilados únicamente para conectar viajes, entregas y garantizar la seguridad dentro de la plataforma."
            )
            TermsSection(
                title = "3. Eliminación de Cuenta y Reglas de Uso",
                body = "• Queda prohibido el uso de la aplicación para fines ilícitos.\n• Los conductores y repartidores deben contar con sus documentos vigentes.\n• Puedes solicitar la eliminación total de tu cuenta y datos enviando un correo a soporte@viakaab.com."
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald, contentColor = PureWhite),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("accept_terms_button")
            ) {
                Text("Entendido y Acepto", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TermsSection(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = body,
                color = Slate300,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
