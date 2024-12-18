package com.example.pakuair

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pakuair.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance() // Instance database

        binding.textLink.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.singUpBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val pass = binding.pass.text.toString()
            val confPass = binding.confPass.text.toString()
            val username = binding.username.text.toString() // Ambil username dari input
            val phone = binding.phone.text.toString() // Ambil nomor telepon dari input

            if (email.isNotEmpty() && pass.isNotEmpty() && confPass.isNotEmpty() && username.isNotEmpty() && phone.isNotEmpty()) {
                if (pass == confPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid // Ambil User ID
                            val user = mapOf(
                                "username" to username,
                                "email" to email,
                                "phone" to phone
                            )

                            // Simpan data ke Firebase Realtime Database
                            userId?.let {
                                database.reference.child("Users").child(it).setValue(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, SignInActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kotak masih belum terisi semua!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}