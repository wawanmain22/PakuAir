package com.example.pakuair.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CekAir(
    val id: String = "",
    val derajatKeasaman: Double = 0.0,
    val kesadahan: Double = 0.0,
    val padatan: Double = 0.0,
    val kloramin: Double = 0.0,
    val sulfat: Double = 0.0,
    val konduktivitas: Double = 0.0,
    val karbonOrganik: Double = 0.0,
    val trihalometan: Double = 0.0,
    val kekeruhan: Double = 0.0
) : Parcelable