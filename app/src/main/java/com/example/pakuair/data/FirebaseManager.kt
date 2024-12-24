package com.example.pakuair.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.pakuair.data.model.User
import com.example.pakuair.data.model.Toko
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.GenericTypeIndicator

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
    private val hasilCekAirRef = database.getReference("hasilCekAir")

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
        println("Fetching toko for user: $userId")

        tokoRef.get()
            .addOnSuccessListener { snapshot ->
                println("Got database snapshot")
                val tokoList = mutableListOf<Toko>()

                snapshot.children.forEach { child ->
                    val toko = child.getValue(Toko::class.java)
                    println("Found toko: ${child.key}, userId: ${toko?.userId}")

                    if (toko?.userId == userId) {
                        tokoList.add(toko)
                    }
                }

                println("Found ${tokoList.size} toko for user")
                onComplete(tokoList)
            }
            .addOnFailureListener { error ->
                println("Error fetching toko: ${error.message}")
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

    fun saveHasilCekAir(
        parameters: Map<String, Double>,
        result: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = getCurrentUser()?.uid ?: return onComplete(false, "User tidak ditemukan")
        
        val hasilCekAir = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "parameters" to parameters,
            "result" to result
        )

        hasilCekAirRef
            .child(userId)
            .push()
            .setValue(hasilCekAir)
            .addOnCompleteListener { task ->
                onComplete(
                    task.isSuccessful,
                    if (task.isSuccessful) null else task.exception?.message
                )
            }
    }

    // Tambahkan method untuk mengambil history
    fun getHasilCekAir(onComplete: (List<HasilCekAir>) -> Unit) {
        val userId = getCurrentUser()?.uid ?: return onComplete(emptyList())
        
        hasilCekAirRef.child(userId)
            .orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val hasilList = mutableListOf<HasilCekAir>()
                    for (child in snapshot.children.reversed()) {
                        try {
                            val timestamp = child.child("timestamp").getValue(Long::class.java) ?: continue
                            val parameters = child.child("parameters")
                                .getValue(object : GenericTypeIndicator<Map<String, Double>>() {}) ?: continue
                            val result = child.child("result")
                                .getValue(object : GenericTypeIndicator<Map<String, Any>>() {}) ?: continue
                            
                            hasilList.add(HasilCekAir(
                                id = child.key ?: "",
                                timestamp = timestamp,
                                parameters = parameters,
                                potability = (result["potability"] as? Long)?.toInt() ?: 0,
                                message = result["message"] as? String ?: "",
                                predictionTime = result["prediction_time"] as? String ?: ""
                            ))
                        } catch (e: Exception) {
                            println("Error parsing hasil: ${e.message}")
                        }
                    }
                    onComplete(hasilList)
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(emptyList())
                }
            })
    }

    fun getHasilCekAirById(hasilId: String, onComplete: (HasilCekAir?) -> Unit) {
        val userId = getCurrentUser()?.uid ?: return onComplete(null)
        
        hasilCekAirRef.child(userId).child(hasilId)
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java)
                    val parameters = snapshot.child("parameters")
                        .getValue(object : GenericTypeIndicator<Map<String, Double>>() {})
                    val result = snapshot.child("result")
                        .getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                    
                    if (timestamp != null && parameters != null && result != null) {
                        val hasil = HasilCekAir(
                            id = snapshot.key ?: "",
                            timestamp = timestamp,
                            parameters = parameters,
                            potability = (result["potability"] as? Long)?.toInt() ?: 0,
                            message = result["message"] as? String ?: "",
                            predictionTime = result["prediction_time"] as? String ?: ""
                        )
                        onComplete(hasil)
                    } else {
                        onComplete(null)
                    }
                } catch (e: Exception) {
                    println("Error parsing hasil: ${e.message}")
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun addHasilCekAirListener(userId: String, onResult: (HasilCekAir?) -> Unit) {
        hasilCekAirRef.child(userId)
            .orderByChild("timestamp")
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        onResult(null)
                        return
                    }

                    try {
                        val lastHasil = snapshot.children.first()
                        val timestamp = lastHasil.child("timestamp").getValue(Long::class.java)
                        val parameters = lastHasil.child("parameters")
                            .getValue(object : GenericTypeIndicator<Map<String, Double>>() {})
                        val result = lastHasil.child("result")
                            .getValue(object : GenericTypeIndicator<Map<String, Any>>() {})

                        if (timestamp != null && parameters != null && result != null) {
                            val hasil = HasilCekAir(
                                id = lastHasil.key ?: "",
                                timestamp = timestamp,
                                parameters = parameters,
                                potability = (result["potability"] as? Long)?.toInt() ?: 0,
                                message = result["message"] as? String ?: "",
                                predictionTime = result["prediction_time"] as? String ?: ""
                            )
                            onResult(hasil)
                        } else {
                            onResult(null)
                        }
                    } catch (e: Exception) {
                        println("Error parsing hasil: ${e.message}")
                        onResult(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error loading hasil: ${error.message}")
                    onResult(null)
                }
            })
    }

    fun getDepotWithGoodWater(onComplete: (List<Pair<Toko, HasilCekAir>>) -> Unit) {
        val depotList = mutableListOf<Pair<Toko, HasilCekAir>>()
        var completedChecks = 0L

        tokoRef.get().addOnSuccessListener { tokoSnapshot ->
            val totalToko = tokoSnapshot.childrenCount
            println("Total toko found: $totalToko") // Tambahkan log

            if (totalToko == 0L) {
                onComplete(emptyList())
                return@addOnSuccessListener
            }

            tokoSnapshot.children.forEach { tokoData ->
                val toko = tokoData.getValue(Toko::class.java) ?: run {
                    completedChecks++
                    if (completedChecks == totalToko) {
                        onComplete(depotList.sortedByDescending { it.second.timestamp })
                    }
                    return@forEach
                }

                hasilCekAirRef.child(toko.userId)
                    .orderByChild("timestamp")
                    .limitToLast(1)
                    .get()
                    .addOnSuccessListener { hasilSnapshot ->
                        if (hasilSnapshot.exists()) {
                            val lastHasil = hasilSnapshot.children.first()
                            try {
                                val result = lastHasil.child("result")
                                    .getValue(object : GenericTypeIndicator<Map<String, Any>>() {})

                                // Penanganan nullable yang lebih aman
                                val potability = result?.let {
                                    (it["potability"] as? Long)?.toInt()
                                } ?: 0

                                if (potability == 1) {
                                    val timestamp = lastHasil.child("timestamp").getValue(Long::class.java)
                                    val parameters = lastHasil.child("parameters")
                                        .getValue(object : GenericTypeIndicator<Map<String, Double>>() {})
                                    val message = result?.get("message") as? String ?: ""
                                    val predictionTime = result?.get("prediction_time") as? String ?: ""

                                    if (timestamp != null && parameters != null) {
                                        val hasil = HasilCekAir(
                                            id = lastHasil.key ?: "",
                                            timestamp = timestamp,
                                            parameters = parameters,
                                            potability = potability,
                                            message = message,
                                            predictionTime = predictionTime
                                        )
                                        depotList.add(Pair(toko, hasil))
                                    }
                                }
                            } catch (e: Exception) {
                                println("Error parsing hasil for toko ${toko.id}: ${e.message}")
                            }
                        }

                        completedChecks++
                        if (completedChecks == totalToko) {
                            onComplete(depotList.sortedByDescending { it.second.timestamp })
                        }
                    }
                    .addOnFailureListener {
                        println("Failed to get hasil for toko ${toko.id}: ${it.message}")
                        completedChecks++
                        if (completedChecks == totalToko) {
                            onComplete(depotList.sortedByDescending { it.second.timestamp })
                        }
                    }
            }
        }.addOnFailureListener {
            println("Failed to get toko list: ${it.message}")
            onComplete(emptyList())
        }
    }
}

// Data class untuk hasil cek air
data class HasilCekAir(
    val id: String,
    val timestamp: Long,
    val parameters: Map<String, Double>,
    val potability: Int,
    val message: String,
    val predictionTime: String
) 