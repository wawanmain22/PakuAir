package com.example.pakuair

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TokoPreviewActivity : AppCompatActivity() {
    private lateinit var loadingProgress: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var storeDetailsLayout: ScrollView
    private lateinit var backButton: ImageView
    private lateinit var btnAddToko: MaterialButton
    private lateinit var btnEditToko: MaterialButton
    private lateinit var ivTokoPhoto: ImageView
    private lateinit var tvNamaToko: TextView
    private lateinit var tvAlamatToko: TextView
    private lateinit var tvContactOwner: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var currentTokoData: TokoData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toko_preview)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        loadingProgress = findViewById(R.id.loadingProgress)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        storeDetailsLayout = findViewById(R.id.storeDetailsLayout)
        backButton = findViewById(R.id.backButton)
        btnAddToko = findViewById(R.id.btnAddToko)
        btnEditToko = findViewById(R.id.btnEditToko)
        ivTokoPhoto = findViewById(R.id.ivTokoPhoto)
        tvNamaToko = findViewById(R.id.tvNamaToko)
        tvAlamatToko = findViewById(R.id.tvAlamatToko)
        tvContactOwner = findViewById(R.id.tvContactOwner)

        // Set click listeners
        backButton.setOnClickListener { finish() }
        btnAddToko.setOnClickListener { navigateToTokoForm(false) }
        btnEditToko.setOnClickListener { navigateToTokoForm(true) }

        // Load toko data
        loadTokoData()
    }

    private fun loadTokoData() {
        val userId = auth.currentUser?.uid ?: return

        showLoading()

        val tokoRef = database.reference.child("toko")
        tokoRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoading()
                    if (snapshot.exists()) {
                        for (tokoSnapshot in snapshot.children) {
                            val tokoData = tokoSnapshot.getValue(TokoData::class.java)
                            if (tokoData != null) {
                                currentTokoData = tokoData
                                showTokoDetails(tokoData)
                                return
                            }
                        }
                    }
                    showEmptyState()
                }

                override fun onCancelled(error: DatabaseError) {
                    hideLoading()
                    Toast.makeText(this@TokoPreviewActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    showEmptyState()
                }
            })
    }

    private fun showTokoDetails(tokoData: TokoData) {
        emptyStateLayout.visibility = View.GONE
        storeDetailsLayout.visibility = View.VISIBLE

        tvNamaToko.text = tokoData.namaToko
        tvAlamatToko.text = tokoData.alamatToko
        tvContactOwner.text = tokoData.contactOwner

        if (tokoData.photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(tokoData.photoUrl)
                .centerCrop()
                .into(ivTokoPhoto)
        }
    }

    private fun showEmptyState() {
        emptyStateLayout.visibility = View.VISIBLE
        storeDetailsLayout.visibility = View.GONE
    }

    private fun showLoading() {
        loadingProgress.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        storeDetailsLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgress.visibility = View.GONE
    }

    private fun navigateToTokoForm(isEdit: Boolean) {
        val intent = Intent(this, TokoPribadiActivity::class.java).apply {
            if (isEdit && currentTokoData != null) {
                putExtra("isEditMode", true)
                putExtra("tokoId", currentTokoData?.id)
            }
        }
        startActivity(intent)
    }
} 