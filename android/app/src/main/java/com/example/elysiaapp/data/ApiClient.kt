package com.example.elysiaapp.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.util.Log

@Serializable
data class HelloResponse(
    val message: String,
    val timestamp: String
)

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val uptime: Double
)

class ApiClient {
    private val baseUrl = "http://10.0.2.2:3000" // Android emulator host
    
    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("ApiClient", message)
                }
            }
            level = LogLevel.INFO
        }
    }

    suspend fun sayHello(name: String = "World"): Result<HelloResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("$baseUrl/api/hello") {
                parameter("name", name)
            }.body<HelloResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Log.e("ApiClient", "Error calling hello endpoint", e)
            Result.failure(e)
        }
    }

    suspend fun checkHealth(): Result<HealthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("$baseUrl/api/health").body<HealthResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Log.e("ApiClient", "Error calling health endpoint", e)
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}