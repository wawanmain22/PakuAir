package com.example.pakuair

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Button
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.pakuair.api.RetrofitClient
import com.example.pakuair.api.WaterQualityRequest
import kotlinx.coroutines.launch

class CekKualitasAirActivity : AppCompatActivity() {

    private lateinit var edtPhAir: EditText
    private lateinit var edtKekerasan: EditText
    private lateinit var edtStd: EditText
    private lateinit var edtKloramin: EditText
    private lateinit var edtSulfate: EditText
    private lateinit var edtKekeruhan: EditText
    private lateinit var edtKarbonOrganic: EditText
    private lateinit var edtTrihalometana: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cek_kualitas)

        // Initialize EditText fields
        edtPhAir = findViewById(R.id.edtPhAir)
        edtKekerasan = findViewById(R.id.edtKekerasan)
        edtStd = findViewById(R.id.edtStd)
        edtKloramin = findViewById(R.id.edtKloramin)
        edtSulfate = findViewById(R.id.edtSulfate)
        edtKekeruhan = findViewById(R.id.edtKekeruhan)
        edtKarbonOrganic = findViewById(R.id.edtKarbonOrganic)
        edtTrihalometana = findViewById(R.id.edtTrihalometana)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            checkWaterQuality()
        }
    }

    private fun checkWaterQuality() {
        try {
            val request = WaterQualityRequest(
                ph = edtPhAir.text.toString().toDouble(),
                hardness = edtKekerasan.text.toString().toDouble(),
                solids = edtStd.text.toString().toDouble(),
                chloramines = edtKloramin.text.toString().toDouble(),
                sulfate = edtSulfate.text.toString().toDouble(),
                conductivity = 0.0, // This field is not in your form
                organicCarbon = edtKarbonOrganic.text.toString().toDouble(),
                trihalomethanes = edtTrihalometana.text.toString().toDouble(),
                turbidity = edtKekeruhan.text.toString().toDouble()
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.waterQualityApi.predictWaterQuality(request)
                    Log.d("CekKualitasAir", "Response: ${response.isSuccessful}, Body: ${response.body()}")
                    
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        Log.d("CekKualitasAir", "Starting HasilKualitasAirActivity with potability: ${result.potability}, message: ${result.message}")
                        
                        startActivity(Intent(this@CekKualitasAirActivity, HasilKualitasAirActivity::class.java).apply {
                            putExtra("potability", result.potability)
                            putExtra("message", result.message)
                        })
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("CekKualitasAir", "Error response: $errorBody")
                        Toast.makeText(this@CekKualitasAirActivity,
                            "Error: $errorBody",
                            Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("CekKualitasAir", "Exception: ${e.message}", e)
                    Toast.makeText(this@CekKualitasAirActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: NumberFormatException) {
            Log.e("CekKualitasAir", "Invalid number format: ${e.message}")
            Toast.makeText(this, "Mohon isi semua field dengan angka yang valid", Toast.LENGTH_LONG).show()
        }
    }
}