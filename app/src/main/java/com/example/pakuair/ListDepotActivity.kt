package com.example.pakuair

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListDepotActivity : AppCompatActivity() {
    private lateinit var rvDepot: RecyclerView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_depot)

        // Initialize views
        rvDepot = findViewById(R.id.rvDepot)
        backButton = findViewById(R.id.backButton)

        // Set up RecyclerView
        rvDepot.layoutManager = LinearLayoutManager(this)
        
        // Sample data
        val depotList = listOf(
            DepotItem(
                nama = "Depot Air Pekanbaru",
                kualitas = "Baik",
                alamat = "Jln Pramuka, No 08",
                kontak = "081978899009"
            ),
            DepotItem(
                nama = "Depot Air Pekanbaru",
                kualitas = "Baik",
                alamat = "Jln Pramuka, No 08",
                kontak = "081978899009"
            ),
            DepotItem(
                nama = "Depot Air Pekanbaru",
                kualitas = "Baik",
                alamat = "Jln Pramuka, No 08",
                kontak = "081978899009"
            ),
            DepotItem(
                nama = "Depot Air Pekanbaru",
                kualitas = "Baik",
                alamat = "Jln Pramuka, No 08",
                kontak = "081978899009"
            )
        )

        // Set adapter
        rvDepot.adapter = DepotAdapter(depotList)

        // Set click listener for back button
        backButton.setOnClickListener {
            finish()
        }
    }
} 