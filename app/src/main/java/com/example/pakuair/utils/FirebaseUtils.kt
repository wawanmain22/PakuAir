package com.example.pakuair.utils

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

object FirebaseUtils {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // Auth Utils
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Database Utils
    fun getUserReference(userId: String = getCurrentUserId() ?: ""): DatabaseReference {
        return database.child("Users").child(userId)
    }

    fun getDepotReference(): DatabaseReference {
        return database.child("Depots")
    }

    // Coroutine Extensions
    suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

    suspend fun DatabaseReference.awaitSingleValue(): DataSnapshot = 
        suspendCancellableCoroutine { continuation ->
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    continuation.resume(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            }
            
            addListenerForSingleValueEvent(valueEventListener)
            
            continuation.invokeOnCancellation {
                removeEventListener(valueEventListener)
            }
        }

    // Network Check
    @Volatile
    private var lastOnlineCheck: Long = 0
    private const val ONLINE_CHECK_INTERVAL = 60_000 // 1 minute

    fun updateOnlineStatus(userId: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastOnlineCheck >= ONLINE_CHECK_INTERVAL) {
            lastOnlineCheck = currentTime
            val userStatusRef = getUserReference(userId).child("status")
            
            // When this node is removed, set offline status
            userStatusRef.onDisconnect().setValue("offline")
            
            // Set online status
            userStatusRef.setValue("online")
            
            // Update last seen
            getUserReference(userId).child("lastSeen")
                .setValue(ServerValue.TIMESTAMP)
        }
    }
} 