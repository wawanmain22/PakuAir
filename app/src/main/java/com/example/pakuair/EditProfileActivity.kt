package com.example.pakuair

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.widget.AppCompatButton

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var edtNama: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtPass: EditText
    private lateinit var saveButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Tombol kembali
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Inisialisasi Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Referensi ke UI
        edtNama = findViewById(R.id.edtNama)
        edtEmail = findViewById(R.id.edtEmail)
        edtPhone = findViewById(R.id.edtPhone)
        saveButton = findViewById(R.id.saveEdtProfile)

        // Ambil data pengguna
        loadData()

        // Tombol simpan
        saveButton.setOnClickListener {
            saveData()
        }
    }

    private fun loadData() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            database.child("Users").child(userId).get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.child("username").value.toString()
                val email = dataSnapshot.child("email").value.toString()
                val phone = dataSnapshot.child("phone").value.toString()

                // Set data ke EditText
                edtNama.setText(username)
                edtEmail.setText(email)
                edtPhone.setText(phone)
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal memuat data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveData() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val newUsername = edtNama.text.toString().trim()
            val newEmail = edtEmail.text.toString().trim()
            val newPhone = edtPhone.text.toString().trim()

            if (newUsername.isNotEmpty() && newEmail.isNotEmpty() && newPhone.isNotEmpty()) {
                val updates = mapOf(
                    "username" to newUsername,
                    "email" to newEmail,
                    "phone" to newPhone,
                )

                database.child("Users").child(userId).updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profil berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                        finish() // Kembali ke halaman sebelumnya
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Gagal memperbarui profil: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
        }
    }
}
