package com.example.elysiaapp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class HelloMessage(val message: String)

fun main() {
    val json = Json { prettyPrint = true }
    val hello = HelloMessage("Hello from Kotlin with Serialization!")
    val jsonString = json.encodeToString(HelloMessage.serializer(), hello)
    println(jsonString)
    
    val parsed = json.decodeFromString(HelloMessage.serializer(), jsonString)
    println("Parsed: ${parsed.message}")
}