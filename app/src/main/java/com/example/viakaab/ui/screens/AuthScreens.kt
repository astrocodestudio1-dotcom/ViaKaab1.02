package com.example.viakaab.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureBlack
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate200
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate600
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
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
    var visible by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.78f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "splash_logo_scale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(620L)
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate950, Slate900, Slate950)
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .alpha(contentAlpha)
        ) {
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .size(174.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandEmerald.copy(alpha = 0.28f),
                                BrandBlue.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_viakaab),
                    contentDescription = "Logo ViaKaab",
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .border(2.dp, BrandEmerald.copy(alpha = 0.65f), RoundedCornerShape(30.dp)),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "ViaKaab",
                color = PureWhite,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = BrandEmerald.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "FELIPE CARRILLO PUERTO · QUINTANA ROO",
                    color = BrandEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }
        }
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
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Correo enviado", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { authViewModel.dismissForgotPasswordDialog() }) {
                    Text("Aceptar", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Recuperar Contraseña", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Ingresa tu correo electrónico para enviarte las instrucciones de restablecimiento de contraseña:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = forgotEmailInput,
                        onValueChange = { forgotEmailInput = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = darkAuthFieldColors(),
                        shape = RoundedCornerShape(14.dp),
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
                    Text("Enviar", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate950, Slate900, Slate950)
                )
            )
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
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(PureWhite)
                    .border(2.dp, BrandEmerald.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_viakaab),
                    contentDescription = "Logo ViaKaab",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bienvenido a ViaKaab",
                color = PureWhite,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Viajes, envíos y traslados en Quintana Roo",
                color = Slate400,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.92f)),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    inlineAuthError?.let { errorText ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.16f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = errorText,
                                color = PureWhite,
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
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = BrandEmerald)
                        },
                        isError = emailError != null,
                        supportingText = emailError?.let { err -> { Text(err) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = darkAuthFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            inlineAuthError = null
                            if (it.trim().isNotEmpty()) passwordError = null
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBlue)
                        },
                        isError = passwordError != null,
                        supportingText = passwordError?.let { err -> { Text(err) } },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Mostrar contraseña",
                                    tint = Slate300
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = darkAuthFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = "¿Olvidé mi contraseña?",
                            color = BrandSkyTint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    forgotEmailInput = email.trim()
                                    showForgotDialog = true
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                                .testTag("forgot_password_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandEmerald,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("login_button")
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        border = BorderStroke(1.dp, Slate600),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("go_to_register_button")
                    ) {
                        Text("Crear cuenta nueva", color = PureWhite, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = BrandEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Conductores verificados · Tarifa justa · SOS 24/7",
                    color = Slate400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
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
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("¡Registro Exitoso!", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tus datos han sido guardados correctamente. A continuación, selecciona cómo deseas usar ViaKaab.")
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.dismissRegisterSuccessDialog()
                    onNavigateToRoleSelection()
                }) {
                    Text("Continuar", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate950, Slate900, Slate950)
                )
            )
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
            Text(
                text = "Únete a ViaKaab",
                style = MaterialTheme.typography.headlineMedium,
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Crea tu perfil para viajar o generar ingresos en Felipe Carrillo Puerto.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.92f)),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    inlineRegisterError?.let { err ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.16f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = err,
                                color = PureWhite,
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
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandEmerald) },
                        isError = nameError != null,
                        supportingText = nameError?.let { err -> { Text(err) } },
                        singleLine = true,
                        colors = darkAuthFieldColors(),
                        shape = RoundedCornerShape(14.dp),
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
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandBlue) },
                        isError = phoneError != null,
                        supportingText = phoneError?.let { err -> { Text(err) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = darkAuthFieldColors(),
                        shape = RoundedCornerShape(14.dp),
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
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = BrandSkyTint) },
                        isError = ageError != null,
                        supportingText = ageError?.let { err -> { Text(err) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = darkAuthFieldColors(),
                        shape = RoundedCornerShape(14.dp),
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
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandEmerald) },
                        isError = emailError != null,
                        supportingText = emailError?.let { err -> { Text(err) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = darkAuthFieldColors(),
                        shape = RoundedCornerShape(14.dp),
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
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBlue) },
                        isError = passwordError != null,
                        supportingText = passwordError?.let { err -> { Text(err) } },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Mostrar contraseña",
                                    tint = Slate300
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = darkAuthFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input")
                    )

                    Text(
                        text = "Al registrarte, aceptas nuestros Términos y Condiciones y Política de Privacidad",
                        color = BrandSkyTint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandEmerald,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("register_submit_button")
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onBackToLogin,
                        border = BorderStroke(1.dp, Slate600),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_back_to_login_button")
                    ) {
                        Text("Ya tengo cuenta (Iniciar Sesión)", color = PureWhite, fontWeight = FontWeight.SemiBold)
                    }
                }
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
            painter = painterResource(id = R.drawable.img_viakaab_role_bg),
            contentDescription = "Fondo ViaKaab Quintana Roo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Slate950.copy(alpha = 0.55f),
                            Slate900.copy(alpha = 0.84f),
                            Slate950.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BrandEmerald.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "FELIPE CARRILLO PUERTO",
                        color = BrandEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Bienvenido a ViaKaab",
                    color = PureWhite,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Elige cómo quieres moverte o generar ingresos hoy",
                    color = Slate300,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleOptionGlassCard(
                    title = "Pasajero",
                    subtitle = "Viaja seguro y propón tu tarifa justa",
                    icon = Icons.Default.PersonPinCircle,
                    accentColor = BrandEmerald,
                    testTag = "role_passenger_button",
                    onClick = { pickRole(UserRole.PASSENGER) }
                )
                RoleOptionGlassCard(
                    title = "Conductor",
                    subtitle = "Acepta viajes locales e interurbanos",
                    icon = Icons.Default.DirectionsCar,
                    accentColor = BrandBlue,
                    testTag = "role_driver_button",
                    onClick = { pickRole(UserRole.DRIVER) }
                )
                RoleOptionGlassCard(
                    title = "Repartidor",
                    subtitle = "Entrega comida, mandados y paquetes",
                    icon = Icons.Default.TwoWheeler,
                    accentColor = BrandSky,
                    testTag = "role_delivery_button",
                    onClick = { pickRole(UserRole.DELIVERY) }
                )
                RoleOptionGlassCard(
                    title = "Ambos (Conductor y Repartidor)",
                    subtitle = "Recibe solicitudes de viajes y entregas",
                    icon = Icons.Default.AllInclusive,
                    accentColor = BrandSkyTint,
                    testTag = "role_both_button",
                    onClick = { pickRole(UserRole.BOTH) }
                )
            }
        }
    }
}

@Composable
private fun RoleOptionGlassCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.88f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.42f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Slate400, fontSize = 12.sp)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun darkAuthFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Slate900,
    unfocusedContainerColor = Slate900.copy(alpha = 0.7f),
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
    focusedBorderColor = BrandEmerald,
    unfocusedBorderColor = Slate700,
    focusedLabelColor = BrandEmerald,
    unfocusedLabelColor = Slate400,
    cursorColor = BrandEmerald
)
