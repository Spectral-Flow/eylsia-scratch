package com.example.elysiaapp.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ChatMessage(
    val type: String,
    val from: String? = null,
    val text: String? = null,
    val message: String? = null,
    val timestamp: String
)

@Serializable
data class OutgoingMessage(
    val from: String,
    val text: String
)

sealed class WebSocketEvent {
    data class MessageReceived(val message: ChatMessage) : WebSocketEvent()
    data class Connected(val message: String) : WebSocketEvent()
    data class Disconnected(val reason: String) : WebSocketEvent()
    data class Error(val error: Throwable) : WebSocketEvent()
}

class WsClient {
    private val baseUrl = "ws://10.0.2.2:3000" // Android emulator host
    private val json = Json { ignoreUnknownKeys = true }
    
    private val httpClient = HttpClient(OkHttp) {
        install(WebSockets)
    }

    fun connect(): Flow<WebSocketEvent> = flow {
        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = "10.0.2.2",
                port = 3000,
                path = "/ws"
            ) {
                emit(WebSocketEvent.Connected("Connected to chat server"))
                Log.d("WsClient", "WebSocket connected")

                try {
                    // Listen for incoming messages
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val messageText = frame.readText()
                                Log.d("WsClient", "Received: $messageText")
                                
                                try {
                                    val chatMessage = json.decodeFromString<ChatMessage>(messageText)
                                    emit(WebSocketEvent.MessageReceived(chatMessage))
                                } catch (e: Exception) {
                                    Log.e("WsClient", "Error parsing message: $messageText", e)
                                    emit(WebSocketEvent.Error(e))
                                }
                            }
                            is Frame.Close -> {
                                Log.d("WsClient", "WebSocket closed")
                                emit(WebSocketEvent.Disconnected("Connection closed"))
                                break
                            }
                            else -> {
                                // Handle other frame types if needed
                            }
                        }
                    }
                } catch (e: ClosedReceiveChannelException) {
                    Log.d("WsClient", "WebSocket channel closed")
                    emit(WebSocketEvent.Disconnected("Channel closed"))
                } catch (e: Exception) {
                    Log.e("WsClient", "WebSocket error", e)
                    emit(WebSocketEvent.Error(e))
                }
            }
        } catch (e: Exception) {
            Log.e("WsClient", "Failed to connect to WebSocket", e)
            emit(WebSocketEvent.Error(e))
        }
    }

    suspend fun sendMessage(from: String, text: String) {
        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = "10.0.2.2",
                port = 3000,
                path = "/ws"
            ) {
                val message = OutgoingMessage(from = from, text = text)
                val messageJson = json.encodeToString(message)
                send(messageJson)
                Log.d("WsClient", "Sent: $messageJson")
            }
        } catch (e: Exception) {
            Log.e("WsClient", "Error sending message", e)
            throw e
        }
    }

    fun close() {
        httpClient.close()
    }
}