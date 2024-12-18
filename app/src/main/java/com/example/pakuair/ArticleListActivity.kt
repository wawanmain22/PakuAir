package com.example.pakuair

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class ArticleListActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView : NavigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_main_menu -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_article_list -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val articles = listOf(
            Article("Faktor yang mempengaruhi kualitas air",
                "faktor yang biasa menjadi penyebab kurangnya kualitas air adalah...", "https://example.com/article1"),
            Article("Dampak polusi terhadap kualitas air",
                "faktor yang biasa menjadi penyebab kurangnya kualitas air adalah...", "https://example.com/article2"),
            Article("Dampak polusi terhadap kualitas air",
                "faktor yang biasa menjadi penyebab kurangnya kualitas air adalah...", "https://example.com/article3"),
            Article("Faktor yang mempengaruhi kualitas air",
                "faktor yang biasa menjadi penyebab kurangnya kualitas air adalah...", "https://example.com/article4"),
        )

        val adapter = ArticleAdapter(articles)
        recyclerView.adapter = adapter
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }
}