package com.example.pakuair.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = ""  // Note: Password should not be stored in Realtime Database in production
) 