package com.example.pakuair

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.pakuair.data.FirebaseManager
import com.example.pakuair.databinding.ActivityMainBinding
import com.example.pakuair.ui.auth.AuthActivity
import androidx.activity.OnBackPressedCallback
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup drawer navigation
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_toko,
                R.id.nav_profile,
            ),
            binding.drawerLayout
        )

        // Setup toolbar dengan navigation
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Tambahkan listener untuk navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.setNavigationOnClickListener {
                when (destination.id) {
                    R.id.nav_home, R.id.nav_toko, R.id.nav_profile -> {
                        binding.drawerLayout.openDrawer(GravityCompat.START)
                    }
                    else -> {
                        navController.navigateUp()
                    }
                }
            }
        }

        // Setup navigation drawer header
        val headerView = binding.navView.getHeaderView(0)
        val userNameText = headerView.findViewById<TextView>(R.id.userNameText)
        FirebaseManager.getCurrentUser()?.let { firebaseUser ->
            FirebaseManager.getUser(firebaseUser.uid) { user ->
                runOnUiThread {
                    user?.let {
                        userNameText.text = it.username
                    }
                }
            }
        }

        // Setup logout menu item
        binding.navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            FirebaseManager.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            true
        }

        // Back press handler
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}