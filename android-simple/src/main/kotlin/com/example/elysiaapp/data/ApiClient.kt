package com.example.elysiaapp.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ApiClient {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // Use 10.0.2.2 for Android emulator to access localhost
    private const val BASE_URL = "http://10.0.2.2:3000"

    @Serializable
    data class HealthResponse(
        val status: String,
        val timestamp: String,
        val requestId: String,
        val service: String,
        val version: String? = null,
        val uptime: Double,
        val services: Services? = null,
        val system: SystemInfo? = null
    )

    @Serializable
    data class Services(
        val elevenLabs: ElevenLabsStatus
    )

    @Serializable
    data class ElevenLabsStatus(
        val available: Boolean,
        val circuitBreaker: CircuitBreakerStatus
    )

    @Serializable
    data class CircuitBreakerStatus(
        val state: String,
        val failureCount: Int,
        val lastFailureTime: Long? = null
    )

    @Serializable
    data class SystemInfo(
        val nodeVersion: String,
        val platform: String,
        val memory: MemoryInfo
    )

    @Serializable
    data class MemoryInfo(
        val rss: Long,
        val heapTotal: Long,
        val heapUsed: Long,
        val external: Long,
        val arrayBuffers: Long
    )

    @Serializable
    data class HelloResponse(
        val message: String,
        val timestamp: String,
        val requestId: String,
        val voiceEnabled: Boolean? = null
    )

    @Serializable
    data class VoiceSynthesisRequest(
        val text: String,
        val voiceId: String? = null,
        val voiceSettings: VoiceSettings? = null
    )

    @Serializable
    data class VoiceSettings(
        val stability: Double? = null,
        val similarityBoost: Double? = null,
        val style: Double? = null,
        val useSpeakerBoost: Boolean? = null
    )

    @Serializable
    data class Voice(
        val id: String,
        val name: String,
        val category: String,
        val description: String
    )

    @Serializable
    data class VoicesResponse(
        val voices: List<Voice>,
        val requestId: String,
        val timestamp: String
    )

    @Serializable
    data class ErrorResponse(
        val error: Boolean,
        val message: String,
        val requestId: String,
        val timestamp: String
    )

    /**
     * Get server health status
     */
    suspend fun getHealth(): Result<HealthResponse> = runCatching {
        client.get("$BASE_URL/health").body<HealthResponse>()
    }

    /**
     * Say hello with enhanced response
     */
    suspend fun sayHello(name: String): Result<HelloResponse> = runCatching {
        client.get("$BASE_URL/api/hello") {
            url {
                parameters.append("name", name)
            }
        }.body<HelloResponse>()
    }

    /**
     * Get available voices for synthesis
     */
    suspend fun getVoices(): Result<VoicesResponse> = runCatching {
        client.get("$BASE_URL/api/voice/voices").body<VoicesResponse>()
    }

    /**
     * Synthesize text to speech
     */
    suspend fun synthesizeVoice(request: VoiceSynthesisRequest): Result<ByteArray> = runCatching {
        client.post("$BASE_URL/api/voice/synthesize") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ByteArray>()
    }

    /**
     * Check if voice synthesis is available
     */
    suspend fun isVoiceAvailable(): Boolean {
        return try {
            val health = getHealth().getOrNull()
            health?.services?.elevenLabs?.available == true
        } catch (e: Exception) {
            false
        }
    }

    fun close() {
        client.close()
    }
}