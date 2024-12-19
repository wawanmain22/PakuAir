package com.example.pakuair

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.example.pakuair.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvLandingUsername: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if user is logged in
        if (!FirebaseUtils.isUserLoggedIn()) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        // Initialize Toolbar and set as action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup DrawerLayout and NavigationView
        setupNavigationDrawer(toolbar)

        // Setup CardView click listeners
        setupCardViewListeners()

        // Load user data
        loadUserData()
    }

    private fun setupNavigationDrawer(toolbar: Toolbar) {
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Get header view from NavigationView
        val headerView = navigationView.getHeaderView(0)
        tvUserName = headerView.findViewById(R.id.tv_user_name)
        tvUserEmail = headerView.findViewById(R.id.tv_user_email)
        tvLandingUsername = findViewById(R.id.tv_landing_username)

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.menu_toko -> {
                    startActivity(Intent(this, TokoPreviewActivity::class.java))
                }
                R.id.menu_logout -> {
                    logoutUser()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupCardViewListeners() {
        findViewById<CardView>(R.id.cardCekKualitas).setOnClickListener {
            startActivity(Intent(this, CekKualitasAirActivity::class.java))
        }

        findViewById<CardView>(R.id.cardDepotInformasi).setOnClickListener {
            startActivity(Intent(this, ListDepotActivity::class.java))
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                FirebaseUtils.getCurrentUserId()?.let { userId ->
                    // Update online status
                    FirebaseUtils.updateOnlineStatus(userId)

                    // Get user data
                    val snapshot = FirebaseUtils.getUserReference(userId).awaitSingleValue()
                    
                    val username = snapshot.child("username").value?.toString() ?: "User"
                    val email = snapshot.child("email").value?.toString() ?: ""

                    // Update UI
                    tvUserName.text = username
                    tvUserEmail.text = email
                    tvLandingUsername.text = "Halo, $username"
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
