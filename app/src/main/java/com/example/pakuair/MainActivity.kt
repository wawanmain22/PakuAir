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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvLandingUsername: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FirebaseAuth and DatabaseReference
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize Toolbar and set as action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // Add toggle for burger menu
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

        // Optionally set username on the main content
        tvLandingUsername = findViewById(R.id.tv_landing_username)

        // Retrieve and listen to user information
        listenToUserData()

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

        // Setup CardView click listeners
        findViewById<CardView>(R.id.cardCekKualitas).setOnClickListener {
            startActivity(Intent(this, CekKualitasAirActivity::class.java))
        }

        findViewById<CardView>(R.id.cardDepotInformasi).setOnClickListener {
            startActivity(Intent(this, ListDepotActivity::class.java))
        }
    }

    private fun listenToUserData() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = database.child("Users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").value.toString()
                    val email = snapshot.child("email").value.toString()

                    // Update UI
                    tvUserName.text = username
                    tvUserEmail.text = email
                    tvLandingUsername.text = "Halo, $username"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Logout user and navigate back to SignInActivity
    private fun logoutUser() {
        firebaseAuth.signOut()
        Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear activity stack
        startActivity(intent)
        finish()
    }
}
