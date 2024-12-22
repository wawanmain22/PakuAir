package com.example.pakuair.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pakuair.data.FirebaseManager

class SigninViewModel : ViewModel() {
    private val _signInResult = MutableLiveData<Result>()
    val signInResult: LiveData<Result> = _signInResult

    fun signIn(email: String, password: String) {
        // Validasi input
        when {
            email.isEmpty() -> {
                _signInResult.value = Result.Error("Email tidak boleh kosong")
                return
            }
            password.isEmpty() -> {
                _signInResult.value = Result.Error("Kata sandi tidak boleh kosong")
                return
            }
        }

        // Proses signin
        FirebaseManager.signIn(email, password) { success, error ->
            if (success) {
                _signInResult.value = Result.Success
            } else {
                _signInResult.value = Result.Error(error ?: "Gagal masuk")
            }
        }
    }

    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }
}