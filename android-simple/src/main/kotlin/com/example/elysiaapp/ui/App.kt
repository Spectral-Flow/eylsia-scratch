package com.example.elysiaapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elysiaapp.data.ApiClient
import com.example.elysiaapp.data.ChatMessage
import com.example.elysiaapp.data.WsClient
import com.example.elysiaapp.ui.components.*
import com.example.elysiaapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme(
        colorScheme = SciFiDarkColorScheme,
        typography = SciFiTypography,
        shapes = SciFiShapes
    ) {
        val scope = rememberCoroutineScope()
        var apiResponse by remember { mutableStateOf("—") }
        var messageInput by remember { mutableStateOf("") }
        var isConnected by remember { mutableStateOf(false) }
        var isVoiceEnabled by remember { mutableStateOf(false) }
        var voiceLoading by remember { mutableStateOf(false) }

        val wsClient = remember { WsClient() }
        val messages by wsClient.messages.collectAsStateWithLifecycle(initialValue = emptyList())
        val connectionState by wsClient.connectionState.collectAsStateWithLifecycle(initialValue = false)
        
        LaunchedEffect(connectionState) {
            isConnected = connectionState
        }

        // Check voice availability on startup
        LaunchedEffect(Unit) {
            isVoiceEnabled = ApiClient.isVoiceAvailable()
        }

        // Animated background
        val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
        val backgroundOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "background_offset"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SciFiColors.DarkBackground,
                            SciFiColors.DarkSurface,
                            SciFiColors.DarkBackground
                        )
                    )
                )
                .drawBehind {
                    drawCyberGrid(backgroundOffset)
                }
        ) {
            Scaffold(
                topBar = {
                    CyberTopBar(
                        isConnected = isConnected,
                        isVoiceEnabled = isVoiceEnabled
                    )
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // System Status Section
                    HolographicCard {
                        Text(
                            text = "SYSTEM STATUS",
                            style = MaterialTheme.typography.titleLarge,
                            color = SciFiColors.NeonCyan,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatusIndicator(
                                isActive = isConnected,
                                label = "CONNECTION"
                            )
                            StatusIndicator(
                                isActive = isVoiceEnabled,
                                label = "VOICE SYNTH"
                            )
                        }
                        
                        GlowingDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // API Testing Controls
                        NeonButton(
                            onClick = {
                                scope.launch {
                                    apiResponse = "SCANNING..."
                                    val result = ApiClient.sayHello("Neural Link")
                                    apiResponse = result.fold(
                                        onSuccess = { "ONLINE - ${it.message} (${it.timestamp})" },
                                        onFailure = { "ERROR - ${it.message}" }
                                    )
                                    isVoiceEnabled = ApiClient.isVoiceAvailable()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SYSTEM DIAGNOSTIC", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = apiResponse,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SciFiColors.TextAccent,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Voice Testing Section
                    if (isVoiceEnabled) {
                        HolographicCard {
                            Text(
                                text = "NEURAL VOICE INTERFACE",
                                style = MaterialTheme.typography.titleLarge,
                                color = SciFiColors.NeonPurple,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            NeonButton(
                                onClick = {
                                    if (!voiceLoading) {
                                        scope.launch {
                                            voiceLoading = true
                                            try {
                                                val result = ApiClient.synthesizeVoice(
                                                    ApiClient.VoiceSynthesisRequest(
                                                        text = "Neural interface activated. Voice synthesis online."
                                                    )
                                                )
                                                result.fold(
                                                    onSuccess = { 
                                                        // Voice synthesis successful
                                                        // In a real app, you would play the audio here
                                                    },
                                                    onFailure = { 
                                                        // Handle error
                                                    }
                                                )
                                            } catch (e: Exception) {
                                                // Handle error
                                            } finally {
                                                voiceLoading = false
                                            }
                                        }
                                    }
                                },
                                enabled = !voiceLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (voiceLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = SciFiColors.NeonPurple,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    if (voiceLoading) "SYNTHESIZING..." else "TEST VOICE SYNTHESIS",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Neural Chat Interface
                    HolographicCard(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "NEURAL CHAT INTERFACE",
                            style = MaterialTheme.typography.titleLarge,
                            color = SciFiColors.NeonGreen,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        GlowingDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = SciFiColors.NeonGreen
                        )

                        // Connection controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NeonButton(
                                onClick = { wsClient.connect() },
                                enabled = !isConnected,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CONNECT", fontWeight = FontWeight.Bold)
                            }

                            NeonButton(
                                onClick = { wsClient.disconnect() },
                                enabled = isConnected,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("DISCONNECT", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Message input
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CyberTextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                label = "Neural Message",
                                modifier = Modifier.weight(1f),
                                enabled = isConnected
                            )

                            NeonButton(
                                onClick = {
                                    if (messageInput.isNotBlank()) {
                                        wsClient.sendMessage(messageInput)
                                        messageInput = ""
                                    }
                                },
                                enabled = isConnected && messageInput.isNotBlank()
                            ) {
                                Text("►", style = MaterialTheme.typography.titleLarge)
                            }

                            // Voice button
                            if (isVoiceEnabled) {
                                NeonButton(
                                    onClick = {
                                        if (messageInput.isNotBlank()) {
                                            wsClient.sendVoiceRequest(messageInput)
                                            messageInput = ""
                                        }
                                    },
                                    enabled = isConnected && messageInput.isNotBlank()
                                ) {
                                    Text("🎤", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }

                        // Messages display
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SciFiColors.DarkBackground.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(messages) { message ->
                                    CyberMessageItem(message = message)
                                }
                            }
                        }
                    }
        
        val listState = rememberLazyListState()

        // Auto-scroll to bottom when new messages arrive
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
        // Cleanup when composable is disposed
        DisposableEffect(Unit) {
            onDispose {
                wsClient.close()
                ApiClient.close()
            }
        }
    }
}

@Composable
private fun CyberTopBar(
    isConnected: Boolean,
    isVoiceEnabled: Boolean
) {
    TopAppBar(
        title = { 
            Column {
                Text(
                    "ELYSIA NEURAL INTERFACE",
                    style = MaterialTheme.typography.titleLarge,
                    color = SciFiColors.NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusIndicator(
                        isActive = isConnected,
                        label = "LINK"
                    )
                    StatusIndicator(
                        isActive = isVoiceEnabled,
                        label = "VOICE"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SciFiColors.DarkSurface.copy(alpha = 0.9f),
            titleContentColor = SciFiColors.TextPrimary,
        ),
        modifier = Modifier.drawBehind {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        SciFiColors.NeonCyan.copy(alpha = 0.3f),
                        Color.Transparent,
                        SciFiColors.NeonPurple.copy(alpha = 0.3f)
                    )
                ),
                size = size
            )
        }
    )
}

@Composable
private fun CyberMessageItem(message: ChatMessage) {
    val messageColor = when (message.type) {
        "system" -> SciFiColors.NeonPurple
        "voice-response" -> SciFiColors.NeonGreen
        "voice-error" -> SciFiColors.ErrorRed
        "history" -> SciFiColors.NeonCyan
        else -> when (message.from) {
            "user" -> SciFiColors.TextPrimary
            "assistant" -> SciFiColors.NeonCyan
            else -> SciFiColors.TextSecondary
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = messageColor.copy(alpha = 0.1f),
                    size = size
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = SciFiColors.DarkCard.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = messageColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Text(
                        text = when (message.type) {
                            "voice-response" -> "🎤 VOICE SYNTH"
                            "voice-error" -> "❌ VOICE ERROR"
                            "history" -> "📚 NEURAL CACHE"
                            "system" -> "🖥 SYSTEM"
                            else -> when (message.from) {
                                "user" -> "👤 USER"
                                "assistant" -> "🤖 AI"
                                else -> message.from?.uppercase() ?: message.type.uppercase()
                            }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = messageColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = SciFiColors.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            when (message.type) {
                "history" -> {
                    message.messages?.forEach { historyMessage ->
                        Text(
                            text = "• ${historyMessage.text ?: historyMessage.message ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SciFiColors.TextSecondary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                "voice-response" -> {
                    Text(
                        text = "Neural voice synthesis completed successfully",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SciFiColors.TextPrimary
                    )
                    if (message.audioData != null) {
                        Text(
                            text = "Audio data: ${message.audioData.length} bytes",
                            style = MaterialTheme.typography.bodySmall,
                            color = SciFiColors.NeonGreen
                        )
                    }
                }
                "voice-error" -> {
                    Text(
                        text = message.error ?: "Neural voice synthesis failed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SciFiColors.ErrorRed
                    )
                }
                else -> {
                    Text(
                        text = message.message ?: message.text ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SciFiColors.TextPrimary
                    )
                }
            }
            
            // Show neural features if available
            message.features?.let { features ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (features.voiceEnabled) {
                        Text(
                            text = "🎤 VOICE_ENABLED",
                            style = MaterialTheme.typography.labelSmall,
                            color = SciFiColors.NeonGreen
                        )
                    }
                    if (features.chatHistory) {
                        Text(
                            text = "📚 CACHE_ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SciFiColors.NeonCyan
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCyberGrid(offset: Float) {
    val gridSize = 50.dp.toPx()
    val cols = (size.width / gridSize).toInt() + 1
    val rows = (size.height / gridSize).toInt() + 1
    
    val animatedOffset = offset * gridSize
    
    // Draw grid lines
    for (i in 0..cols) {
        val x = (i * gridSize - animatedOffset) % (size.width + gridSize)
        drawLine(
            color = SciFiColors.BorderGlow.copy(alpha = 0.1f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    for (i in 0..rows) {
        val y = (i * gridSize - animatedOffset) % (size.height + gridSize)
        drawLine(
            color = SciFiColors.BorderGlow.copy(alpha = 0.1f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}