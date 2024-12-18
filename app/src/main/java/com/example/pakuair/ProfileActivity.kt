package com.example.pakuair

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener

    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profilePhone: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Tombol kembali
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Tombol Edit Profile
        findViewById<AppCompatButton>(R.id.editProfileButton).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Inisialisasi Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Referensi ke UI
        profileName = findViewById(R.id.profileName)
        profileEmail = findViewById(R.id.profileEmail)
        profilePhone = findViewById(R.id.profilePhone)

        // Ambil userId dan setup listener
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            userRef = database.child("Users").child(userId)
            setupUserDataListener()
        } else {
            Toast.makeText(this, "Pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUserDataListener() {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ambil data dan perbarui UI
                val username = snapshot.child("username").value.toString()
                val email = snapshot.child("email").value.toString()
                val phone = snapshot.child("phone").value.toString()

                profileName.text = username
                profileEmail.text = email
                profilePhone.text = phone
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Tambahkan listener ke referensi pengguna
        userRef.addValueEventListener(valueEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hapus listener untuk mencegah kebocoran memori
        userRef.removeEventListener(valueEventListener)
    }
}
