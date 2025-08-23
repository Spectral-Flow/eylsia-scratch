package com.example.elysiaapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elysiaapp.data.ApiClient
import com.example.elysiaapp.data.ChatMessage
import com.example.elysiaapp.data.WsClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
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

        val listState = rememberLazyListState()

        // Auto-scroll to bottom when new messages arrive
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Elysia Demo")
                            Text(
                                "Voice: ${if (isVoiceEnabled) "Enabled" else "Disabled"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isVoiceEnabled) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // API Testing Section
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "API Testing",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    apiResponse = "Loading..."
                                    val result = ApiClient.sayHello("Richie")
                                    apiResponse = result.fold(
                                        onSuccess = { "${it.message} (${it.timestamp}) - Voice: ${it.voiceEnabled}" },
                                        onFailure = { "Error: ${it.message}" }
                                    )
                                    // Update voice status
                                    isVoiceEnabled = ApiClient.isVoiceAvailable()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ping Server")
                        }

                        Text(
                            text = "Response: $apiResponse",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Voice Testing Section
                if (isVoiceEnabled) {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Voice Synthesis",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Button(
                                onClick = {
                                    if (!voiceLoading) {
                                        scope.launch {
                                            voiceLoading = true
                                            try {
                                                val result = ApiClient.synthesizeVoice(
                                                    ApiClient.VoiceSynthesisRequest(
                                                        text = "Hello from Android app! This is a voice synthesis test."
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
                                    Text("Synthesizing...")
                                } else {
                                    Text("Test Voice Synthesis")
                                }
                            }
                        }
                    }
                }

                // WebSocket Chat Section
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "WebSocket Chat",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Connection controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { wsClient.connect() },
                                enabled = !isConnected
                            ) {
                                Text("Connect")
                            }

                            Button(
                                onClick = { wsClient.disconnect() },
                                enabled = isConnected
                            ) {
                                Text("Disconnect")
                            }

                            Text(
                                text = if (isConnected) "Connected" else "Disconnected",
                                color = if (isConnected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }

                        // Message input
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                label = { Text("Message") },
                                modifier = Modifier.weight(1f),
                                enabled = isConnected
                            )

                            Button(
                                onClick = {
                                    if (messageInput.isNotBlank()) {
                                        wsClient.sendMessage(messageInput)
                                        messageInput = ""
                                    }
                                },
                                enabled = isConnected && messageInput.isNotBlank()
                            ) {
                                Text("Send")
                            }

                            // Voice button
                            if (isVoiceEnabled) {
                                Button(
                                    onClick = {
                                        if (messageInput.isNotBlank()) {
                                            wsClient.sendVoiceRequest(messageInput)
                                            messageInput = ""
                                        }
                                    },
                                    enabled = isConnected && messageInput.isNotBlank()
                                ) {
                                    Text("🎤")
                                }
                            }
                        }

                        // Messages list
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(messages) { message ->
                                    MessageItem(message = message)
                                }
                            }
                        }
                    }
                }
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
private fun MessageItem(message: ChatMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (message.type) {
                "system" -> MaterialTheme.colorScheme.secondaryContainer
                "voice-response" -> MaterialTheme.colorScheme.primaryContainer
                "voice-error" -> MaterialTheme.colorScheme.errorContainer
                "history" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (message.type) {
                        "voice-response" -> "🎤 Voice Response"
                        "voice-error" -> "❌ Voice Error"
                        "history" -> "📚 History"
                        else -> message.from ?: message.type
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            when (message.type) {
                "history" -> {
                    message.messages?.forEach { historyMessage ->
                        Text(
                            text = "• ${historyMessage.text ?: historyMessage.message ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                "voice-response" -> {
                    Text(
                        text = "Voice synthesis completed",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (message.audioData != null) {
                        Text(
                            text = "Audio data received (${message.audioData.length} chars)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                "voice-error" -> {
                    Text(
                        text = message.error ?: "Voice synthesis failed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Text(
                        text = message.message ?: message.text ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Show voice features if available
            message.features?.let { features ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (features.voiceEnabled) {
                        Text(
                            text = "🎤 Voice",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (features.chatHistory) {
                        Text(
                            text = "📚 History",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}