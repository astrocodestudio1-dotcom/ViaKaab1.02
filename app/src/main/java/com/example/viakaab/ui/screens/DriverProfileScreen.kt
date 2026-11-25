package com.example.viakaab.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.model.UserRole
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.LightSurface
import com.example.viakaab.ui.theme.PureBlack
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate200
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate600
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.viewmodel.MainAppViewModel

@Composable
fun DriverProfileScreen(
    viewModel: MainAppViewModel,
    onBack: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenFaceVerification: () -> Unit,
    onOpenIneVerification: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsStateWithLifecycle()
    val docStatus by viewModel.docStatus.collectAsStateWithLifecycle()
    val savedVehicle by viewModel.vehicleInfo.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    var brand by rememberSaveable { mutableStateOf(savedVehicle.brand) }
    var model by rememberSaveable { mutableStateOf(savedVehicle.model) }
    var year by rememberSaveable { mutableStateOf(savedVehicle.year) }
    var plate by rememberSaveable { mutableStateOf(savedVehicle.plate) }
    var vehicleType by rememberSaveable { mutableStateOf(savedVehicle.vehicleType) }
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }
    var showRoleDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(savedVehicle) {
        if (brand.isEmpty() && savedVehicle.brand.isNotEmpty()) brand = savedVehicle.brand
        if (model.isEmpty() && savedVehicle.model.isNotEmpty()) model = savedVehicle.model
        if (year.isEmpty() && savedVehicle.year.isNotEmpty()) year = savedVehicle.year
        if (plate.isEmpty() && savedVehicle.plate.isNotEmpty()) plate = savedVehicle.plate
        if (vehicleType.isEmpty() && savedVehicle.vehicleType.isNotEmpty()) vehicleType = savedVehicle.vehicleType
    }

    val inePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadDocument("ine", it.toString()) }
    }
    val licensePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadDocument("license", it.toString()) }
    }
    val insurancePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadDocument("insurance", it.toString()) }
    }

    LaunchedEffect(actionState.message) {
        actionState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas salir?") },
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

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Cambiar modo de uso") },
            text = {
                Column {
                    UserRole.entries.forEach { role ->
                        OutlinedButton(
                            onClick = {
                                showRoleDialog = false
                                onSwitchRole(role)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
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

    val roleDisplay = when (session.role) {
        UserRole.DELIVERY -> "Repartidor"
        UserRole.BOTH -> "Conductor y Repartidor"
        else -> "Conductor"
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
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Slate900)
                }
                Text(
                    text = "Panel de $roleDisplay",
                    color = Slate900,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Nombre: ${session.effectiveDisplayName}",
                        color = Slate900,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Correo: ${session.email.ifBlank { "No disponible" }}",
                        color = Slate600,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Modo: $roleDisplay · ★ 4.9",
                        color = BrandSky,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Progreso de verificación: ${docStatus.completedStepsCount} / 4 pasos",
                        color = Slate600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { docStatus.completedStepsCount / 4f },
                        color = BrandEmerald,
                        trackColor = Slate200,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onOpenEditProfile,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar Perfil Personal (Nombre, Foto, Teléfono)", color = Slate900)
                    }
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
                    Text(
                        text = if (savedVehicle.isRegistered) {
                            "🚗 Vehículo Registrado (${savedVehicle.brand} ${savedVehicle.model})"
                        } else {
                            "🚗 Registro de Vehículo"
                        },
                        color = Slate900,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    DriverTextField(brand, { brand = it }, "Marca (ej. Nissan, Honda, Italika)")
                    DriverTextField(model, { model = it }, "Modelo (ej. Versa, Civic, DM150)")
                    DriverTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = "Año (ej. 2022)",
                        keyboardType = KeyboardType.Number
                    )
                    DriverTextField(plate, { plate = it.uppercase() }, "Placa o Matrícula")
                    DriverTextField(vehicleType, { vehicleType = it }, "Tipo (auto, moto, bicicleta)")

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val b = brand.trim()
                            val m = model.trim()
                            val y = year.trim().toIntOrNull()
                            val p = plate.trim()
                            val vt = vehicleType.trim()
                            if (b.isEmpty() || m.isEmpty() || p.isEmpty() || vt.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Por favor completa todos los datos del vehículo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (y == null || y < 1980 || y > 2027) {
                                Toast.makeText(context, "Ingresa un año válido", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.registerVehicle(b, m, y, p, vt)
                            }
                        },
                        enabled = !actionState.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = PureWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_vehicle_button")
                    ) {
                        Text("Guardar Vehículo", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📄 Licencia y Documentos Legales",
                        color = Slate900,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sube tus documentos vigentes para validación en Felipe Carrillo Puerto.",
                        color = Slate500,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DocumentUploadBlock(
                        title = "🪪 Credencial INE (Identificación Oficial)",
                        buttonLabel = "Subir Foto de INE",
                        statusText = docStatus.ineStatus,
                        isCompleted = docStatus.ineUploaded,
                        onUploadClick = {
                            inePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )

                    OutlinedButton(
                        onClick = onOpenIneVerification,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("open_ine_camera_button")
                    ) {
                        Text("Escanear INE con Cámara (Frente y Reverso)", color = BrandSky)
                    }

                    DocumentUploadBlock(
                        title = "🪪 Licencia de Conducir",
                        buttonLabel = "Subir Foto de Licencia de Conducir",
                        statusText = docStatus.licenseStatus,
                        isCompleted = docStatus.licenseUploaded,
                        onUploadClick = {
                            licensePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )

                    DocumentUploadBlock(
                        title = "🛡️ Póliza de Seguro del Vehículo",
                        buttonLabel = "Subir Póliza de Seguro (Opcional)",
                        statusText = docStatus.insuranceStatus,
                        isCompleted = docStatus.insuranceUploaded,
                        neutralColor = true,
                        onUploadClick = {
                            insurancePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🤳 Verificación Facial (Biometría)",
                        color = Slate900,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedButton(
                        onClick = onOpenFaceVerification,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_face_verification_button")
                    ) {
                        Text(
                            text = if (docStatus.faceVerified) "Rostro Verificado ✓" else "Iniciar Verificación Facial",
                            color = if (docStatus.faceVerified) BrandEmerald else BrandSky
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showRoleDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("driver_switch_role_button")
            ) {
                Text("Cambiar modo (Pasajero / Conductor / Repartidor)", color = Slate900)
            }

            Text(
                text = "Centro de Soporte 24/7",
                color = BrandSky,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSupport)
                    .padding(vertical = 12.dp)
            )

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
                    .testTag("driver_logout_button")
            )
        }
    }
}

@Composable
private fun DriverTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureBlack,
            unfocusedTextColor = PureBlack,
            focusedBorderColor = Slate900,
            unfocusedBorderColor = Slate500
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun DocumentUploadBlock(
    title: String,
    buttonLabel: String,
    statusText: String,
    isCompleted: Boolean,
    neutralColor: Boolean = false,
    onUploadClick: () -> Unit
) {
    Text(
        text = title,
        color = Slate900,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
    OutlinedButton(
        onClick = onUploadClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        Text(buttonLabel, color = Slate900)
    }
    Text(
        text = statusText,
        color = when {
            isCompleted -> BrandEmerald
            neutralColor -> Slate500
            else -> BrandRed
        },
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 14.dp)
    )
}
