package com.example.pakuair.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pakuair.MainActivity
import com.example.pakuair.R
import com.example.pakuair.data.FirebaseManager
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        // Check if user is already signed in
        if (isSessionValid()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_container, SignInFragment())
                .commit()
        }
    }

    private fun isSessionValid(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser == null) return false

        // Get last sign in timestamp
        val lastSignInTimestamp = currentUser.metadata?.lastSignInTimestamp ?: 0
        val currentTime = System.currentTimeMillis()
        
        // Session expires after 30 days of inactivity
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val isSessionExpired = (currentTime - lastSignInTimestamp) > thirtyDaysInMillis

        return !isSessionExpired
    }
} 