package com.example.elysiaapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elysiaapp.data.ApiClient
import com.example.elysiaapp.data.ChatMessage
import com.example.elysiaapp.data.WebSocketEvent
import com.example.elysiaapp.data.WsClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class MessageDisplayModel(
    val type: String,
    val from: String,
    val text: String,
    val timestamp: String
)

data class AppUiState(
    val isLoading: Boolean = false,
    val serverResponse: String = "",
    val error: String = "",
    val messages: List<MessageDisplayModel> = emptyList(),
    val connectionStatus: String = "Disconnected"
)

class AppViewModel : ViewModel() {
    private val apiClient = ApiClient()
    private val wsClient = WsClient()
    
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    fun pingServer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = "",
                serverResponse = ""
            )
            
            try {
                val result = apiClient.sayHello("Richie")
                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            serverResponse = "✅ ${response.message}\n🕐 ${response.timestamp}"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "❌ ${exception.message ?: "Unknown error"}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "❌ ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun connectToWebSocket() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(connectionStatus = "Connecting")
            
            wsClient.connect()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        connectionStatus = "Error",
                        error = "WebSocket error: ${error.message}"
                    )
                }
                .collect { event ->
                    when (event) {
                        is WebSocketEvent.Connected -> {
                            _uiState.value = _uiState.value.copy(
                                connectionStatus = "Connected",
                                error = ""
                            )
                        }
                        
                        is WebSocketEvent.MessageReceived -> {
                            val message = event.message
                            val displayMessage = MessageDisplayModel(
                                type = message.type,
                                from = message.from ?: "System",
                                text = message.text ?: message.message ?: "Unknown message",
                                timestamp = formatTimestamp(message.timestamp)
                            )
                            
                            _uiState.value = _uiState.value.copy(
                                messages = _uiState.value.messages + displayMessage
                            )
                        }
                        
                        is WebSocketEvent.Disconnected -> {
                            _uiState.value = _uiState.value.copy(
                                connectionStatus = "Disconnected"
                            )
                        }
                        
                        is WebSocketEvent.Error -> {
                            _uiState.value = _uiState.value.copy(
                                connectionStatus = "Error",
                                error = "WebSocket error: ${event.error.message}"
                            )
                        }
                    }
                }
        }
    }

    fun sendMessage(from: String, text: String) {
        viewModelScope.launch {
            try {
                wsClient.sendMessage(from, text)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to send message: ${e.message}"
                )
            }
        }
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val instant = Instant.parse(timestamp)
            timeFormatter.format(instant)
        } catch (e: Exception) {
            timestamp.substringAfter("T").substringBefore(".")
        }
    }

    override fun onCleared() {
        super.onCleared()
        apiClient.close()
        wsClient.close()
    }
}