package com.example.pakuair.data

import com.example.pakuair.data.model.CekAir
import com.example.pakuair.data.model.HasilCekAir
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
import android.util.Log

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
        Log.d("FirebaseManager", "Getting toko with id: $tokoId")
        
        tokoRef.child(tokoId).get()
            .addOnSuccessListener { snapshot ->
                Log.d("FirebaseManager", "Toko snapshot exists: ${snapshot.exists()}")
                
                val toko = snapshot.getValue(Toko::class.java)
                Log.d("FirebaseManager", "Parsed toko: ${toko?.namaToko}")
                
                onComplete(toko)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseManager", "Failed to get toko: ${e.message}")
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

    fun addHasilCekAirListener(userId: String, onDataChange: (HasilCekAir?) -> Unit) {
        Log.d("FirebaseManager", "Adding hasil listener for user: $userId")
        
        hasilCekAirRef.child(userId)
            .orderByChild("timestamp")
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseManager", "Hasil snapshot exists: ${snapshot.exists()}")
                    
                    if (snapshot.exists()) {
                        try {
                            val lastHasilData = snapshot.children.first()
                            val hasil = HasilCekAir(
                                id = lastHasilData.key ?: "",
                                userId = userId,
                                timestamp = lastHasilData.child("timestamp").getValue(Long::class.java) ?: 0L,
                                parameters = lastHasilData.child("parameters")
                                    .getValue(object : GenericTypeIndicator<Map<String, Double>>() {}) ?: emptyMap(),
                                potability = (lastHasilData.child("result/potability").getValue(Long::class.java) ?: 0L).toInt(),
                                message = lastHasilData.child("result/message").getValue(String::class.java) ?: "",
                                predictionTime = lastHasilData.child("result/prediction_time").getValue(String::class.java) ?: ""
                            )
                            Log.d("FirebaseManager", "Parsed hasil with potability: ${hasil.potability}")
                            onDataChange(hasil)
                        } catch (e: Exception) {
                            Log.e("FirebaseManager", "Error parsing hasil: ${e.message}")
                            onDataChange(null)
                        }
                    } else {
                        Log.d("FirebaseManager", "No hasil found")
                        onDataChange(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseManager", "Hasil listener cancelled: ${error.message}")
                    onDataChange(null)
                }
            })
    }

    fun getDepotWithGoodWater(onComplete: (List<Pair<Toko, HasilCekAir>>) -> Unit) {
        val goodDepots = mutableListOf<Pair<Toko, HasilCekAir>>()
        
        tokoRef.get().addOnSuccessListener { tokoSnapshot ->
            Log.d("FirebaseManager", "Toko snapshot exists: ${tokoSnapshot.exists()}")
            Log.d("FirebaseManager", "Toko count: ${tokoSnapshot.childrenCount}")
            
            var processedCount: Long = 0L
            val totalCount = tokoSnapshot.childrenCount
            
            if (totalCount == 0L) {
                Log.d("FirebaseManager", "No toko found in database")
                onComplete(emptyList())
                return@addOnSuccessListener
            }

            tokoSnapshot.children.forEach { tokoData ->
                val toko = tokoData.getValue(Toko::class.java)
                Log.d("FirebaseManager", "Processing toko: ${toko?.namaToko}, userId: ${toko?.userId}")
                
                if (toko == null) {
                    processedCount++
                    Log.e("FirebaseManager", "Failed to parse toko data")
                    if (processedCount == totalCount) {
                        onComplete(goodDepots)
                    }
                    return@forEach
                }

                hasilCekAirRef.child(toko.userId)
                    .orderByChild("timestamp")
                    .limitToLast(1)
                    .get()
                    .addOnSuccessListener { hasilSnapshot ->
                        Log.d("FirebaseManager", "Hasil check exists for ${toko.namaToko}: ${hasilSnapshot.exists()}")
                        
                        if (hasilSnapshot.exists()) {
                            val lastHasilData = hasilSnapshot.children.first()
                            
                            try {
                                val timestamp = lastHasilData.child("timestamp").getValue(Long::class.java) ?: 0L
                                val parameters = lastHasilData.child("parameters")
                                    .getValue(object : GenericTypeIndicator<Map<String, Double>>() {}) ?: emptyMap()
                                val result = lastHasilData.child("result")
                                    .getValue(object : GenericTypeIndicator<Map<String, Any>>() {}) ?: emptyMap()
                                
                                val potability = (result["potability"] as? Long)?.toInt() ?: 0
                                Log.d("FirebaseManager", "Toko ${toko.namaToko} potability: $potability")
                                
                                if (potability == 1) { // Air layak minum
                                    val hasil = HasilCekAir(
                                        id = lastHasilData.key ?: "",
                                        userId = toko.userId,
                                        timestamp = timestamp,
                                        parameters = parameters,
                                        potability = potability,
                                        message = result["message"] as? String ?: "",
                                        predictionTime = result["prediction_time"] as? String ?: ""
                                    )
                                    goodDepots.add(Pair(toko, hasil))
                                    Log.d("FirebaseManager", "Added good depot: ${toko.namaToko}")
                                }
                            } catch (e: Exception) {
                                Log.e("FirebaseManager", "Error parsing hasil for ${toko.namaToko}: ${e.message}")
                            }
                        }
                        
                        processedCount++
                        Log.d("FirebaseManager", "Processed count: $processedCount / $totalCount")
                        if (processedCount == totalCount) {
                            Log.d("FirebaseManager", "Completing with ${goodDepots.size} good depots")
                            onComplete(goodDepots)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseManager", "Failed to get hasil for ${toko.namaToko}: ${e.message}")
                        processedCount++
                        if (processedCount == totalCount) {
                            onComplete(goodDepots)
                        }
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseManager", "Failed to get toko list: ${e.message}")
            onComplete(emptyList())
        }
    }
}
