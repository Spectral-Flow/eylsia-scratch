package com.example.elysiaapp.data

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ChatMessage(
    val type: String,
    val message: String? = null,
    val text: String? = null,
    val from: String? = null,
    val timestamp: String
)

class WsClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var client: HttpClient? = null
    private var session: DefaultClientWebSocketSession? = null

    private val _messages = MutableSharedFlow<ChatMessage>()
    val messages: SharedFlow<ChatMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableSharedFlow<Boolean>()
    val connectionState: SharedFlow<Boolean> = _connectionState.asSharedFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Use 10.0.2.2 for Android emulator to access localhost
    private val wsUrl = "ws://10.0.2.2:3000/ws"

    fun connect() {
        if (client != null && session != null) return

        client = HttpClient(OkHttp) {
            install(WebSockets) {
                pingInterval = 20.seconds
            }
        }

        scope.launch {
            try {
                client!!.webSocket(urlString = wsUrl) {
                    session = this
                    _connectionState.emit(true)

                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()
                                    try {
                                        val message = json.decodeFromString<ChatMessage>(text)
                                        _messages.emit(message)
                                    } catch (e: Exception) {
                                        // If parsing fails, create a simple message
                                        _messages.emit(
                                            ChatMessage(
                                                type = "message",
                                                text = text,
                                                timestamp = System.currentTimeMillis().toString()
                                            )
                                        )
                                    }
                                }
                                else -> { /* Handle other frame types if needed */ }
                            }
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        // Connection closed normally
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        _connectionState.emit(false)
                        session = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _connectionState.emit(false)
            } finally {
                client?.close()
                client = null
                session = null
            }
        }
    }

    fun sendMessage(text: String) {
        scope.launch {
            try {
                session?.send(text)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                session?.close()
                client?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                session = null
                client = null
                _connectionState.emit(false)
            }
        }
    }

    fun close() {
        disconnect()
        scope.cancel()
    }
}