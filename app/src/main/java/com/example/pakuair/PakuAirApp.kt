package com.example.pakuair

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class PakuAirApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Enable Firebase Database persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
} 