package com.example.pakuair.data.model

data class HasilCekAir(
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val cekAir: CekAir,
    val potability: Int,
    val message: String,
    val predictionTime: String,
    val systemInfo: Map<String, String>
)