package com.example.pakuair

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pakuair.databinding.ActivityRiwayatKualitasAirBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class RiwayatKualitas : AppCompatActivity() {
    private lateinit var binding: ActivityRiwayatKualitasAirBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatKualitasAirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dummyData = listOf(
            Entry(1f,3f),
            Entry(2f,4f),
            Entry(3f,2f),
            Entry(4f,8f),
            Entry(5f,6f)
        )

        val dataSet = LineDataSet(dummyData, "Kualitas Air")
        dataSet.color = getColor(R.color.blue)
        dataSet.valueTextColor = getColor(R.color.black)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 5f
        dataSet.setCircleColors(getColor(R.color.blue))

        val lineData = LineData(dataSet)

        binding.LineChart.data = lineData


    }
}