package com.example.viakaab.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.R
import com.example.viakaab.model.UserRole
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.PureBlack
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate200
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.ui.theme.Slate950
import com.example.viakaab.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToDriverMap: () -> Unit
) {
    val session by authViewModel.session.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(500L)
        if (session.isLoggedIn) {
            if (session.role.isDriverRole) {
                onNavigateToDriverMap()
            } else {
                onNavigateToMain()
            }
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_viakaab),
            contentDescription = "Logo Viakaab",
            modifier = Modifier.size(width = 300.dp, height = 340.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToDriverMap: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val session by authViewModel.session.collectAsStateWithLifecycle()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var email by rememberSaveable { mutableStateOf(session.savedEmail) }
    var password by rememberSaveable { mutableStateOf(session.savedPassword) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var inlineAuthError by rememberSaveable { mutableStateOf<String?>(null) }
    var showForgotDialog by rememberSaveable { mutableStateOf(false) }
    var forgotEmailInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(session.savedEmail, session.savedPassword) {
        if (email.isEmpty() && session.savedEmail.isNotEmpty()) email = session.savedEmail
        if (password.isEmpty() && session.savedPassword.isNotEmpty()) password = session.savedPassword
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            if (msg.contains("Bienvenido", ignoreCase = true)) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                inlineAuthError = null
            } else {
                inlineAuthError = msg
            }
            authViewModel.clearStatusMessage()
        }
    }

    uiState.forgotPasswordDialogMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { authViewModel.dismissForgotPasswordDialog() },
            title = { Text("Correo enviado") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { authViewModel.dismissForgotPasswordDialog() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Recuperar Contraseña") },
            text = {
                Column {
                    Text(
                        "Ingresa tu correo electrónico para enviarte las instrucciones de restablecimiento de contraseña:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = forgotEmailInput,
                        onValueChange = { forgotEmailInput = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = forgotEmailInput.trim()
                    if (trimmed.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
                        Toast.makeText(context, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                    } else {
                        showForgotDialog = false
                        authViewModel.resetPassword(trimmed)
                    }
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
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
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_viakaab),
                contentDescription = "Logo ViaKaab",
                modifier = Modifier.size(width = 184.dp, height = 148.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(20.dp))

            inlineAuthError?.let { errorText ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = errorText,
                        color = BrandRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    inlineAuthError = null
                    if (it.trim().isNotEmpty()) emailError = null
                },
                label = { Text("Correo Electrónico") },
                isError = emailError != null,
                supportingText = emailError?.let { err -> { Text(err) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = lightOutlinedFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    inlineAuthError = null
                    if (it.trim().isNotEmpty()) passwordError = null
                },
                label = { Text("Contraseña") },
                isError = passwordError != null,
                supportingText = passwordError?.let { err -> { Text(err) } },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar contraseña",
                            tint = Slate900
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = lightOutlinedFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "¿Olvidé mi contraseña?",
                    color = BrandSky,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            forgotEmailInput = email.trim()
                            showForgotDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                        .testTag("forgot_password_button")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPass = password.trim()
                    var hasError = false
                    if (trimmedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                        emailError = "Ingresa un correo electrónico válido"
                        hasError = true
                    }
                    if (trimmedPass.isEmpty()) {
                        passwordError = "Por favor ingresa tu contraseña"
                        hasError = true
                    }
                    if (!hasError) {
                        authViewModel.login(trimmedEmail, trimmedPass) { role ->
                            if (role.isDriverRole) onNavigateToDriverMap() else onNavigateToMain()
                        }
                    }
                },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = PureWhite,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Iniciar Sesión", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToRegister,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("go_to_register_button")
            ) {
                Text("Registrarse", color = Slate900, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToRoleSelection: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var ageError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var inlineRegisterError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            inlineRegisterError = msg
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            authViewModel.clearStatusMessage()
        }
    }

    if (uiState.registerSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Registro Exitoso!") },
            text = {
                Text("Tus datos han sido guardados correctamente. A continuación, selecciona cómo deseas usar ViaKaab.")
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.dismissRegisterSuccessDialog()
                    onNavigateToRoleSelection()
                }) {
                    Text("Continuar")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
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
            Image(
                painter = painterResource(id = R.drawable.logo_viakaab),
                contentDescription = "Logo ViaKaab",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(width = 190.dp, height = 100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineSmall,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ingresa tus datos para comenzar a usar ViaKaab.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            inlineRegisterError?.let { err ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = err,
                        color = BrandRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    inlineRegisterError = null
                    if (it.trim().isNotEmpty()) nameError = null
                },
                label = { Text("Nombre completo") },
                isError = nameError != null,
                supportingText = nameError?.let { err -> { Text(err) } },
                singleLine = true,
                colors = lightOutlinedFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_name_input")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    if (it.trim().isNotEmpty()) phoneError = null
                },
                label = { Text("Número telefónico (10 dígitos)") },
                isError = phoneError != null,
                supportingText = phoneError?.let { err -> { Text(err) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = lightOutlinedFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_phone_input")
            )

            OutlinedTextField(
                value = age,
                onValueChange = {
                    age = it
                    if (it.trim().isNotEmpty()) ageError = null
                },
                label = { Text("Edad (años)") },
                isError = ageError != null,
                supportingText = ageError?.let { err -> { Text(err) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = lightOutlinedFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_age_input")
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    inlineRegisterError = null
                    if (it.trim().isNotEmpty()) emailError = null
                },
                label = { Text("Correo Electrónico") },
                isError = emailError != null,
                supportingText = emailError?.let { err -> { Text(err) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = lightOutlinedFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (it.trim().isNotEmpty()) passwordError = null
                },
                label = { Text("Contraseña (mín. 6 caracteres)") },
                isError = passwordError != null,
                supportingText = passwordError?.let { err -> { Text(err) } },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar contraseña",
                            tint = Slate900
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = lightOutlinedFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_input")
            )

            Text(
                text = "Al registrarte, aceptas nuestros Términos y Condiciones y Política de Privacidad",
                color = BrandSky,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToTerms)
                    .padding(vertical = 12.dp)
            )

            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedPhone = phone.trim()
                    val trimmedAge = age.trim()
                    val ageNum = trimmedAge.toIntOrNull()
                    val trimmedEmail = email.trim()
                    val trimmedPass = password.trim()
                    var hasError = false

                    if (trimmedName.length < 2) {
                        nameError = "Ingresa tu nombre completo"
                        hasError = true
                    }
                    if (trimmedPhone.filter { it.isDigit() }.length < 10) {
                        phoneError = "Ingresa un número telefónico de al menos 10 dígitos"
                        hasError = true
                    }
                    if (ageNum == null || ageNum < 16 || ageNum > 100) {
                        ageError = "Ingresa una edad válida (16 a 100 años)"
                        hasError = true
                    }
                    if (trimmedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                        emailError = "Ingresa un correo electrónico válido (ej. correo@dominio.com)"
                        hasError = true
                    }
                    if (trimmedPass.length < 6) {
                        passwordError = "La contraseña debe tener al menos 6 caracteres"
                        hasError = true
                    }

                    if (!hasError) {
                        authViewModel.register(trimmedName, trimmedPhone, trimmedAge, trimmedEmail, trimmedPass)
                    }
                },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_submit_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = PureWhite,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Registrarse", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackToLogin,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_back_to_login_button")
            ) {
                Text("Ya tengo cuenta (Iniciar Sesión)", color = Slate900, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RoleSelectionScreen(
    authViewModel: AuthViewModel,
    onNavigateToPassengerHome: () -> Unit,
    onNavigateToDriverMap: () -> Unit
) {
    val context = LocalContext.current

    fun pickRole(role: UserRole) {
        authViewModel.selectRole(role)
        Toast.makeText(context, "¡Rol seleccionado: ${role.displayName}!", Toast.LENGTH_SHORT).show()
        if (role.isDriverRole) {
            onNavigateToDriverMap()
        } else {
            onNavigateToPassengerHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.logo_viakaab),
            contentDescription = "Fondo ViaKaab",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Slate950.copy(alpha = 0.72f),
                            Slate900.copy(alpha = 0.88f),
                            Slate950.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Bienvenido a ViaKaab",
                    color = PureWhite,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "¿Cómo quieres usar ViaKaab?",
                    color = Slate200,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RolePillButton(
                    text = "Conductor",
                    testTag = "role_driver_button",
                    onClick = { pickRole(UserRole.DRIVER) }
                )
                RolePillButton(
                    text = "Repartidor",
                    testTag = "role_delivery_button",
                    onClick = { pickRole(UserRole.DELIVERY) }
                )
                RolePillButton(
                    text = "Pasajero",
                    testTag = "role_passenger_button",
                    onClick = { pickRole(UserRole.PASSENGER) }
                )
                RolePillButton(
                    text = "Ambos (Conductor y Repartidor)",
                    testTag = "role_both_button",
                    onClick = { pickRole(UserRole.BOTH) }
                )
            }
        }
    }
}

@Composable
private fun RolePillButton(
    text: String,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = PureWhite.copy(alpha = 0.22f),
            contentColor = PureWhite
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag(testTag)
    ) {
        Text(text = text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun lightOutlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = PureBlack,
    unfocusedTextColor = PureBlack,
    focusedBorderColor = Slate900,
    unfocusedBorderColor = Slate500,
    focusedLabelColor = Slate900,
    unfocusedLabelColor = Slate500,
    cursorColor = Slate900
)
