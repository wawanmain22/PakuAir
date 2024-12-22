package com.example.pakuair.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.pakuair.data.model.User
import com.example.pakuair.data.model.Toko

object FirebaseManager {
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val database: FirebaseDatabase by lazy { 
        Firebase.database.apply {
            setPersistenceEnabled(true)  // Enable offline persistence
        }
    }

    // References
    private val usersRef = database.getReference("users")
    private val tokoRef = database.getReference("toko")

    // Auth Methods
    fun getCurrentUser() = auth.currentUser

    fun signUp(email: String, password: String, username: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: return@addOnCompleteListener
                    val user = User(
                        id = userId,
                        email = email,
                        username = username
                    )
                    saveUser(user) { success ->
                        onComplete(success, if (success) null else "Failed to save user data")
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun signIn(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    val errorMessage = when (task.exception?.message) {
                        "The password is invalid or the user does not have a password." -> 
                            "Kata sandi salah"
                        "There is no user record corresponding to this identifier. The user may have been deleted." -> 
                            "Email tidak terdaftar"
                        "The email address is badly formatted." ->
                            "Format email tidak valid"
                        "The supplied auth credential is incorrect, malformed or has expired." ->
                            "Email atau kata sandi salah"
                        "A network error (such as timeout, interrupted connection or unreachable host) has occurred." ->
                            "Gagal terhubung ke server, periksa koneksi internet Anda"
                        "The email address is already in use by another account." ->
                            "Email sudah terdaftar"
                        "Password should be at least 6 characters" ->
                            "Kata sandi minimal 6 karakter"
                        "Too many unsuccessful login attempts. Please try again later." ->
                            "Terlalu banyak percobaan login gagal. Silakan coba lagi nanti"
                        else -> task.exception?.message ?: "Gagal masuk"
                    }
                    onComplete(false, errorMessage)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    // Database Methods
    private fun saveUser(user: User, onComplete: (Boolean) -> Unit) {
        usersRef.child(user.id).setValue(user)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun updateUsername(userId: String, newUsername: String, onComplete: (Boolean, String?) -> Unit) {
        usersRef.child(userId).child("username").setValue(newUsername)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) null else task.exception?.message)
            }
    }

    fun updatePassword(currentPassword: String, newPassword: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "User tidak ditemukan")
            return
        }

        // Re-authenticate user first
        val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    // After re-authentication, update password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            onComplete(updateTask.isSuccessful, 
                                if (updateTask.isSuccessful) null 
                                else updateTask.exception?.message)
                        }
                } else {
                    onComplete(false, "Password saat ini salah")
                }
            }
    }

    fun saveToko(toko: Toko, onComplete: (Boolean, String?) -> Unit) {
        val tokoId = tokoRef.push().key ?: return onComplete(false, "Failed to generate toko ID")
        val tokoWithId = toko.copy(id = tokoId)
        
        tokoRef.child(tokoId).setValue(tokoWithId)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) null else task.exception?.message)
            }
    }

    fun updateToko(toko: Toko, onComplete: (Boolean, String?) -> Unit) {
        if (toko.id.isEmpty()) {
            onComplete(false, "Invalid toko ID")
            return
        }

        tokoRef.child(toko.id).setValue(toko)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) null else task.exception?.message)
            }
    }

    fun getUser(userId: String, onComplete: (User?) -> Unit) {
        usersRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.getValue(User::class.java))
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun getToko(tokoId: String, onComplete: (Toko?) -> Unit) {
        tokoRef.child(tokoId).get()
            .addOnSuccessListener { snapshot ->
                val toko = snapshot.getValue(Toko::class.java)
                // Pastikan hanya mengembalikan toko milik user yang sedang login
                if (toko != null && toko.userId == getCurrentUser()?.uid) {
                    onComplete(toko)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun getTokoByUser(userId: String, onComplete: (List<Toko>) -> Unit) {
        // Pastikan hanya mengambil toko milik user yang sedang login
        if (userId != getCurrentUser()?.uid) {
            onComplete(emptyList())
            return
        }

        tokoRef.orderByChild("userId").equalTo(userId).get()
            .addOnSuccessListener { snapshot ->
                val tokoList = mutableListOf<Toko>()
                snapshot.children.forEach { child ->
                    child.getValue(Toko::class.java)?.let { toko ->
                        // Double check untuk memastikan hanya toko milik user yang diambil
                        if (toko.userId == userId) {
                            tokoList.add(toko)
                        }
                    }
                }
                onComplete(tokoList)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    // Session Management
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun isSessionValid(): Boolean {
        val currentUser = auth.currentUser ?: return false

        // Get last sign in timestamp
        val lastSignInTimestamp = currentUser.metadata?.lastSignInTimestamp ?: 0
        val currentTime = System.currentTimeMillis()
        
        // Session expires after 30 days of inactivity
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        return (currentTime - lastSignInTimestamp) <= thirtyDaysInMillis
    }

    fun refreshUserSession(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(false)
            return
        }

        // Get a new ID token to refresh the session
        currentUser.getIdToken(true)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
} 