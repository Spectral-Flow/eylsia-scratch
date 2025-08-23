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

        val wsClient = remember { WsClient() }
        val messages by wsClient.messages.collectAsStateWithLifecycle(initialValue = emptyList())
        val connectionState by wsClient.connectionState.collectAsStateWithLifecycle(initialValue = false)
        
        LaunchedEffect(connectionState) {
            isConnected = connectionState
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
                    title = { Text("Elysia Demo") },
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
                                        onSuccess = { "${it.message} (${it.timestamp})" },
                                        onFailure = { "Error: ${it.message}" }
                                    )
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
                    text = message.from ?: message.type,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text = message.message ?: message.text ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}