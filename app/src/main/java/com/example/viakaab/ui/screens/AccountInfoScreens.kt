package com.example.viakaab.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
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
import com.example.viakaab.model.PaymentMethodType
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.LightSurface
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate600
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.viewmodel.MainAppViewModel

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
            title = { Text("Registrar Referencia de Tarjeta") },
            text = {
                Column {
                    Text(
                        "Para tu seguridad, solo guardamos un alias y los últimos 4 dígitos para identificar tu pago con terminal o transferencia.",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cardAlias,
                        onValueChange = { cardAlias = it },
                        label = { Text("Alias (ej. Débito Personal)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardBrand,
                        onValueChange = { cardBrand = it },
                        label = { Text("Red (Visa, Mastercard)") },
                        singleLine = true,
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
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCardDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showSpeiDialog) {
        val clabe = "646180110400000001"
        AlertDialog(
            onDismissRequest = { showSpeiDialog = false },
            title = { Text("Transferencia Bancaria SPEI") },
            text = {
                Column {
                    Text("Usa estos datos para recargar o pagar al conductor por SPEI:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Beneficiario: ViaKaab Movilidad Local", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Institución: STP (Sistema de Transferencias)", fontSize = 13.sp)
                    Text("CLABE: $clabe", fontWeight = FontWeight.Bold, color = BrandSky, fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(clabe))
                    viewModel.selectPaymentMethod(PaymentMethodType.SPEI)
                    Toast.makeText(context, "CLABE copiada y SPEI seleccionado", Toast.LENGTH_SHORT).show()
                    showSpeiDialog = false
                }) {
                    Text("Copiar CLABE y Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSpeiDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSurface)
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
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Slate900)
                }
                Text("Métodos de Pago", color = Slate900, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("👛 Método activo: ${selectedPayment.title}", color = Slate400, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$0.00 MXN Crédito ViaKaab", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            PaymentMethodRowCard(
                title = "💵 Efectivo (Pago directo)",
                badgeText = if (selectedPayment == PaymentMethodType.CASH) "Activo ✓" else "Seleccionar",
                badgeColor = if (selectedPayment == PaymentMethodType.CASH) BrandEmerald else BrandSky,
                onClick = { viewModel.selectPaymentMethod(PaymentMethodType.CASH) }
            )

            val cardTitle = savedCard?.let { "💳 ${it.brandLabel} •••• ${it.last4}" }
                ?: "💳 Tarjeta de Crédito / Débito"
            PaymentMethodRowCard(
                title = cardTitle,
                badgeText = if (selectedPayment == PaymentMethodType.CARD) "Activo ✓" else "+ Configurar",
                badgeColor = if (selectedPayment == PaymentMethodType.CARD) BrandEmerald else BrandSky,
                onClick = { showCardDialog = true }
            )

            PaymentMethodRowCard(
                title = "🏦 Transferencia Bancaria SPEI",
                badgeText = if (selectedPayment == PaymentMethodType.SPEI) "Activo ✓" else "+ Ver CLABE",
                badgeColor = if (selectedPayment == PaymentMethodType.SPEI) BrandEmerald else BrandSky,
                onClick = { showSpeiDialog = true }
            )
        }
    }
}

@Composable
private fun PaymentMethodRowCard(
    title: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
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
            Text(
                text = title,
                color = Slate900,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(text = badgeText, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun SupportScreen(
    onBack: () -> Unit,
    onOpenTerms: () -> Unit
) {
    val context = LocalContext.current
    var expandedFaqIndex by rememberSaveable { mutableIntStateOf(-1) }

    val faqs = listOf(
        "¿Cómo cambio mi método de pago?" to
            "Entra a Cuenta ➔ Billetera o Perfil ➔ Métodos de pago y elige entre Efectivo, Tarjeta o Transferencia SPEI.",
        "¿Qué hago si olvidé un objeto en el viaje?" to
            "Contáctanos de inmediato por correo a soporte@viakaab.com o llama al +52 800 842 5222 con la hora y ruta de tu viaje.",
        "¿Cómo cambio mi rol a Conductor o Repartidor?" to
            "En Cuenta o Mi Perfil toca 'Cambiar rol', elige Conductor, Repartidor o Ambos y completa tus documentos en el Panel de Conductor."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSurface)
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
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Slate900)
                }
                Text("Soporte y Ayuda", color = Slate900, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("¿Cómo podemos ayudarte?", color = Slate900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Nuestro equipo de atención al cliente está disponible 24/7 para resolver cualquier duda sobre viajes, entregas o privacidad.",
                        color = Slate600,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("📧 Correo electrónico:", color = Slate900, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "soporte@viakaab.com",
                        color = BrandSky,
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
                    Text("📞 Teléfono de asistencia:", color = Slate900, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "+52 800 842 5222",
                        color = BrandSky,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:+528008425222"))
                                    )
                                }
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Preguntas Frecuentes", color = Slate900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    faqs.forEachIndexed { index, (question, answer) ->
                        val expanded = expandedFaqIndex == index
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedFaqIndex = if (expanded) -1 else index }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = question,
                                    color = Slate900,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (expanded) "Contraer" else "Expandir",
                                    tint = Slate600
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Text(
                                    text = answer,
                                    color = Slate600,
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("support_view_terms_button")
            ) {
                Text("Consultar Términos y Políticas", color = Slate900, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
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
            Image(
                painter = painterResource(id = R.drawable.logo_viakaab),
                contentDescription = "Logo ViaKaab",
                modifier = Modifier
                    .size(width = 160.dp, height = 80.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Términos y Condiciones y Política de Privacidad",
                color = Slate900,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Última actualización: 2026\nViaKaab - Felipe Carrillo Puerto, Quintana Roo, México",
                color = Slate500,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 24.dp)
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
                colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
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
    Text(
        text = title,
        color = Slate900,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        text = body,
        color = Slate700,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 20.dp)
    )
}
