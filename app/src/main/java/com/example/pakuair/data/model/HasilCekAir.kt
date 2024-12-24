package com.example.pakuair.data.model

data class HasilCekAir(
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val parameters: Map<String, Double> = emptyMap(),
    val potability: Int = 0,
    val message: String = "",
    val predictionTime: String = "",
    val systemInfo: Map<String, String> = emptyMap()
)