package com.example.pakuair

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Button
import android.content.Intent
import android.widget.TextView

class HasilKualitasAirActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hasil_kualitas)

        // Get results from intent
        val potability = intent.getIntExtra("potability", -1)
        val message = intent.getStringExtra("message") ?: "Tidak ada hasil"

        // Update UI with results
        val resultTextView = findViewById<TextView>(R.id.resultTextView)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)

        // Set status text and color based on potability
        val status = when (potability) {
            1 -> "Baik"
            0 -> "Tidak Layak"
            else -> "Error"
        }
        
        val statusColor = when (potability) {
            1 -> getColor(android.R.color.holo_green_dark)
            0 -> getColor(android.R.color.holo_red_dark)
            else -> getColor(android.R.color.black)
        }

        statusTextView.text = status
        statusTextView.setTextColor(statusColor)
        descriptionTextView.text = message

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.homeBotton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}

