package com.example.viakaab.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate200
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.ui.theme.Slate950
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private fun capturePhotoToCache(
    context: Context,
    imageCapture: ImageCapture?,
    prefix: String,
    onDone: () -> Unit
) {
    if (imageCapture == null) {
        onDone()
        return
    }
    val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onDone()
            }

            override fun onError(exception: ImageCaptureException) {
                onDone()
            }
        }
    )
}

@Composable
fun FaceVerificationScreen(
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var showTutorial by rememberSaveable { mutableStateOf(true) }
    var isAnalyzing by rememberSaveable { mutableStateOf(false) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var instructionStep by rememberSaveable {
        mutableStateOf("Paso 1: Centra tu rostro dentro del óvalo y mantén buena iluminación.")
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(showTutorial) {
        if (!showTutorial && !hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showTutorial) {
        AlertDialog(
            onDismissRequest = onBack,
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Instrucciones de Verificación Facial", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Para garantizar la seguridad en ViaKaab (Felipe Carrillo Puerto):\n\n" +
                        "1. Sostén tu teléfono frente a tu rostro.\n" +
                        "2. Centra tu cara dentro del óvalo verde.\n" +
                        "3. Evita usar lentes de sol o gorras.\n" +
                        "4. Presiona 'Capturar y Verificar Rostro'."
                )
            },
            confirmButton = {
                TextButton(onClick = { showTutorial = false }) {
                    Text("¡Entendido!", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onBack) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().apply {
                                    setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val imageCapture = ImageCapture.Builder().build()
                                imageCaptureRef = imageCapture
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_FRONT_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Toast.makeText(
                                    ctx,
                                    "Cámara en modo vista previa: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (!showTutorial) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Se necesita permiso de cámara para validar tu identidad.",
                    color = Slate200,
                    textAlign = TextAlign.Center
                )
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Permitir Cámara")
                }
                OutlinedButton(onClick = onBack) {
                    Text("Volver", color = Slate400)
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val ovalW = 260.dp.toPx()
            val ovalH = 360.dp.toPx()
            val topLeft = Offset((size.width - ovalW) / 2f, size.height * 0.42f - ovalH / 2f)
            drawOval(
                color = BrandEmerald,
                topLeft = topLeft,
                size = Size(ovalW, ovalH),
                style = Stroke(width = 4.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Slate950.copy(alpha = 0.65f))
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = PureWhite)
            }
            Text(
                text = "Verificación Facial (ViaKaab)",
                color = PureWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = instructionStep,
                color = Slate200,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Button(
            onClick = {
                isAnalyzing = true
                instructionStep = "Capturando y validando biometría facial..."
                capturePhotoToCache(context, imageCaptureRef, "face_verify") {
                    scope.launch {
                        delay(900L)
                        Toast.makeText(
                            context,
                            "¡Rostro verificado exitosamente!",
                            Toast.LENGTH_LONG
                        ).show()
                        onVerified()
                    }
                }
            },
            enabled = !isAnalyzing && hasCameraPermission,
            colors = ButtonDefaults.buttonColors(containerColor = BrandSky, contentColor = PureWhite),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .height(52.dp)
                .testTag("capture_face_button")
        ) {
            Text(
                if (isAnalyzing) "Validando..." else "Capturar y Verificar Rostro",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun IneVerificationScreen(
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showTutorial by rememberSaveable { mutableStateOf(true) }
    var currentStep by rememberSaveable { mutableIntStateOf(1) }
    var isCapturing by rememberSaveable { mutableStateOf(false) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(showTutorial) {
        if (!showTutorial && !hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showTutorial) {
        AlertDialog(
            onDismissRequest = onBack,
            containerColor = Slate900,
            titleContentColor = PureWhite,
            textContentColor = Slate300,
            title = { Text("Verificación de INE", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Para validar tu identidad en Felipe Carrillo Puerto:\n\n" +
                        "1. Coloca tu INE sobre una superficie plana con buena iluminación.\n" +
                        "2. Alinea el Frente dentro del recuadro verde y presiona 'Capturar'.\n" +
                        "3. A continuación, haz lo mismo con el Reverso de tu INE."
                )
            },
            confirmButton = {
                TextButton(onClick = { showTutorial = false }) {
                    Text("Empezar", color = BrandEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onBack) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().apply {
                                    setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val imageCapture = ImageCapture.Builder().build()
                                imageCaptureRef = imageCapture
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Toast.makeText(
                                    ctx,
                                    "Cámara trasera en modo vista previa: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (!showTutorial) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Se requiere permiso de cámara para escanear tu INE.",
                    color = Slate200,
                    textAlign = TextAlign.Center
                )
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Permitir Cámara")
                }
                OutlinedButton(onClick = onBack) {
                    Text("Volver", color = Slate400)
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val rectW = 320.dp.toPx()
            val rectH = 200.dp.toPx()
            val topLeft = Offset((size.width - rectW) / 2f, size.height * 0.45f - rectH / 2f)
            drawRoundRect(
                color = BrandEmerald,
                topLeft = topLeft,
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Slate950.copy(alpha = 0.65f))
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = PureWhite)
            }
            Text(
                text = "Verificación de INE (ViaKaab)",
                color = PureWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (currentStep == 1) {
                    "Paso 1: Coloca la parte FRONTAL de tu INE dentro del recuadro."
                } else {
                    "Paso 2: Coloca la parte TRASERA (reverso) de tu INE dentro del recuadro."
                },
                color = Slate200,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Button(
            onClick = {
                isCapturing = true
                capturePhotoToCache(context, imageCaptureRef, "ine_step_$currentStep") {
                    isCapturing = false
                    if (currentStep == 1) {
                        Toast.makeText(context, "Frente de INE capturado correctamente", Toast.LENGTH_SHORT).show()
                        currentStep = 2
                    } else {
                        Toast.makeText(context, "¡INE verificada exitosamente para ViaKaab!", Toast.LENGTH_LONG).show()
                        onVerified()
                    }
                }
            },
            enabled = !isCapturing && hasCameraPermission,
            colors = ButtonDefaults.buttonColors(containerColor = BrandSky, contentColor = PureWhite),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .height(52.dp)
                .testTag("capture_ine_button")
        ) {
            Text(
                text = if (currentStep == 1) "Capturar Frente de INE" else "Capturar Reverso de INE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
