package com.example.pakuair

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pakuair.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.signInBtn.setOnClickListener{
            val email = binding.email.text.toString()
            val pass = binding.pass.text.toString()


            if (email.isNotEmpty() && pass.isNotEmpty() ) {
                    firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener{
                        if(it.isSuccessful){
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            }  else {
                Toast.makeText(this, "Kotak masih belum terisi semua!!" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
