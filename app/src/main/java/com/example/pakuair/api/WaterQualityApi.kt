package com.example.pakuair.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

data class WaterQualityRequest(
    val ph: Double,
    @SerializedName("Hardness") val hardness: Double,
    @SerializedName("Solids") val solids: Double,
    @SerializedName("Chloramines") val chloramines: Double,
    @SerializedName("Sulfate") val sulfate: Double,
    @SerializedName("Conductivity") val conductivity: Double,
    @SerializedName("Organic_carbon") val organicCarbon: Double,
    @SerializedName("Trihalomethanes") val trihalomethanes: Double,
    @SerializedName("Turbidity") val turbidity: Double
)

data class WaterQualityResponse(
    val potability: Int,
    val message: String
)

interface WaterQualityApi {
    @POST("predict")
    suspend fun predictWaterQuality(@Body request: WaterQualityRequest): Response<WaterQualityResponse>
} 