package com.example.viakaab.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.viakaab.model.UserRole
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.LightSurface
import com.example.viakaab.ui.theme.PureBlack
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate600
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.viewmodel.MainAppViewModel

@Composable
fun ProfileScreen(
    viewModel: MainAppViewModel,
    onBack: () -> Unit,
    onOpenDriverProfile: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    onOpenSupport: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }
    var showTripsDialog by rememberSaveable { mutableStateOf(false) }
    var showRoleDialog by rememberSaveable { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout()
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

    if (showTripsDialog) {
        AlertDialog(
            onDismissRequest = { showTripsDialog = false },
            title = { Text("Mis Viajes (${trips.size})") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (trips.isEmpty()) {
                        Text("Aún no tienes viajes registrados en esta cuenta.")
                    } else {
                        trips.forEach { trip ->
                            Column {
                                Text(
                                    text = "${trip.origin} ➔ ${trip.destination}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${trip.fareMxn} · ${trip.timestampLabel} · ${trip.statusLabel}",
                                    fontSize = 12.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTripsDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Seleccionar modo de uso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { role ->
                        OutlinedButton(
                            onClick = {
                                showRoleDialog = false
                                onSwitchRole(role)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val check = if (role == session.role) " ✓" else ""
                            Text("${role.displayName}$check")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Cancelar")
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
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Slate900)
                }
                Text(
                    text = "Mi perfil",
                    color = Slate900,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(BrandSkyTint),
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
                        color = BrandSky,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = session.effectiveDisplayName,
                color = Slate900,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "${session.role.displayName} · ★ 5.0 · ${trips.size} viajes",
                color = Slate500,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )

            if (session.role.isDriverRole) {
                val panelTitle = when (session.role) {
                    UserRole.DRIVER -> "🚗 Panel de Conductor"
                    UserRole.DELIVERY -> "🛵 Panel de Repartidor"
                    UserRole.BOTH -> "🚗🛵 Panel de Conductor y Repartidor"
                    else -> "🚗 Panel de Conductor / Repartidor"
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable(onClick = onOpenDriverProfile)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(panelTitle, color = PureWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Registra tu vehículo, placas, licencia e INE.",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { showTripsDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mis viajes", color = Slate900, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Consulta viajes, tarifas y comprobantes.", color = Slate500, fontSize = 13.sp)
                    }
                    Text("${trips.size}", color = BrandEmerald, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Datos personales", color = Slate900, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nombre: ${session.effectiveDisplayName}", color = Slate600, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Correo: ${session.email.ifBlank { "No disponible" }}",
                        color = Slate600,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Teléfono: ${session.phone.ifBlank { "Sin registrar" }}",
                        color = Slate600,
                        fontSize = 14.sp
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable(onClick = onOpenEditProfile)
                    .testTag("profile_edit_card")
            ) {
                Text(
                    text = "Editar perfil",
                    color = Slate900,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clickable { showRoleDialog = true }
            ) {
                Text(
                    text = "Cambiar rol (Actual: ${session.role.displayName})",
                    color = BrandSky,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                text = "Métodos de pago (Efectivo, Tarjeta, Transferencia SPEI)",
                color = Slate900,
                fontSize = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenPaymentMethods)
                    .padding(vertical = 12.dp)
            )

            Text(
                text = "Soporte",
                color = Slate900,
                fontSize = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSupport)
                    .padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cerrar sesión",
                color = BrandRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutConfirm = true }
                    .padding(vertical = 14.dp)
                    .testTag("profile_logout_button")
            )
        }
    }
}

@Composable
fun EditProfileScreen(
    viewModel: MainAppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    var name by rememberSaveable { mutableStateOf(session.name) }
    var phone by rememberSaveable { mutableStateOf(session.phone) }
    var selectedImageUri by rememberSaveable { mutableStateOf(session.profileImageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedImageUri = it.toString() }
    }

    LaunchedEffect(actionState.message) {
        actionState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSurface)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Slate900)
                }
                Text("Editar Perfil", color = Slate900, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BrandSkyTint)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri.isNotBlank()) {
                    AsyncImage(
                        model = Uri.parse(selectedImageUri),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = session.initials,
                        color = BrandSky,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Cambiar foto de perfil",
                color = BrandSky,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PureBlack,
                    unfocusedTextColor = PureBlack,
                    focusedBorderColor = Slate900,
                    unfocusedBorderColor = Slate500
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("edit_profile_name_input")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PureBlack,
                    unfocusedTextColor = PureBlack,
                    focusedBorderColor = Slate900,
                    unfocusedBorderColor = Slate500
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
                    .testTag("edit_profile_phone_input")
            )

            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.updateProfile(trimmedName, phone.trim(), selectedImageUri, onSuccess = onBack)
                    }
                },
                enabled = !actionState.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button")
            ) {
                Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
