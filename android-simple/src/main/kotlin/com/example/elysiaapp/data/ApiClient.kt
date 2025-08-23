package com.example.elysiaapp.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
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
        val uptime: Double
    )

    @Serializable
    data class HelloResponse(
        val message: String,
        val timestamp: String,
        val requestId: String
    )

    suspend fun getHealth(): Result<HealthResponse> = runCatching {
        client.get("$BASE_URL/health").body<HealthResponse>()
    }

    suspend fun sayHello(name: String): Result<HelloResponse> = runCatching {
        client.get("$BASE_URL/api/hello") {
            url {
                parameters.append("name", name)
            }
        }.body<HelloResponse>()
    }

    fun close() {
        client.close()
    }
}