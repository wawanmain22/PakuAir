package com.example.pakuair.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pakuair.data.FirebaseManager

class SignupViewModel : ViewModel() {
    private val _signUpResult = MutableLiveData<Result>()
    val signUpResult: LiveData<Result> = _signUpResult

    fun signUp(email: String, username: String, password: String, confirmPassword: String) {
        // Validasi input
        when {
            email.isEmpty() -> {
                _signUpResult.value = Result.Error("Email tidak boleh kosong")
                return
            }
            username.isEmpty() -> {
                _signUpResult.value = Result.Error("Nama pengguna tidak boleh kosong")
                return
            }
            password.isEmpty() -> {
                _signUpResult.value = Result.Error("Kata sandi tidak boleh kosong")
                return
            }
            password != confirmPassword -> {
                _signUpResult.value = Result.Error("Konfirmasi kata sandi tidak sesuai")
                return
            }
            password.length < 6 -> {
                _signUpResult.value = Result.Error("Kata sandi minimal 6 karakter")
                return
            }
        }

        // Proses signup
        FirebaseManager.signUp(email, password, username) { success, error ->
            if (success) {
                _signUpResult.value = Result.Success
            } else {
                _signUpResult.value = Result.Error(error ?: "Gagal mendaftar")
            }
        }
    }

    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }
}