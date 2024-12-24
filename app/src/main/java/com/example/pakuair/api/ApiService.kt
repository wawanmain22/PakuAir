package com.example.pakuair.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ApiService {
    private val client = OkHttpClient()
    private val baseUrl = "http://10.0.2.2:5000"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun checkWaterQuality(waterParams: Map<String, Double>): JSONObject = suspendCoroutine { continuation ->
        try {
            val requestBody = JSONObject().apply {
                put("ph", waterParams["derajatKeasaman"])
                put("Hardness", waterParams["kesadahan"])
                put("Solids", waterParams["padatan"])
                put("Chloramines", waterParams["kloramin"])
                put("Sulfate", waterParams["sulfat"])
                put("Conductivity", waterParams["konduktivitas"])
                put("Organic_carbon", waterParams["karbonOrganik"])
                put("Trihalomethanes", waterParams["trihalometan"])
                put("Turbidity", waterParams["kekeruhan"])
            }.toString()

            val request = Request.Builder()
                .url("$baseUrl/predict")
                .post(requestBody.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Request failed: ${response.code}")
                }
                val responseBody = response.body?.string() ?: throw Exception("Empty response")
                continuation.resume(JSONObject(responseBody))
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}