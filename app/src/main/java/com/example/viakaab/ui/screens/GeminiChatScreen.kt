package com.example.viakaab.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate200
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.viewmodel.GeminiChatViewModel

@Composable
fun GeminiChatScreen(
    viewModel: GeminiChatViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var promptText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = PureWhite
                    )
                }
                Text(
                    text = "🤖 Asistente IA (Gemini)",
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!uiState.isFirebaseConnected) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Aviso: Las funciones en vivo de Gemini requieren activar Firebase en la configuración del proyecto.",
                        color = BrandSkyTint,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Slate800, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    val bubbleColor = when {
                        msg.isError -> BrandRed.copy(alpha = 0.2f)
                        msg.isUser -> BrandBlue
                        else -> Slate700
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = bubbleColor),
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (msg.isError) BrandRed else Slate200,
                                fontSize = 15.sp,
                                lineHeight = 21.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            CircularProgressIndicator(
                                color = BrandBlue,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini está pensando...", color = Slate400, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = { Text("Pregúntale a Gemini...", color = Slate400) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = Slate800,
                        cursorColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gemini_prompt_input")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        val trimmed = promptText.trim()
                        if (trimmed.isNotEmpty()) {
                            promptText = ""
                            viewModel.sendPrompt(trimmed)
                        }
                    },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = PureWhite),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("gemini_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}
