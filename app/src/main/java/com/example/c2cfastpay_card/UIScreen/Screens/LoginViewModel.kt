// 【修正 1：修改 Package 名稱】
// 原本是 package com.example.c2cfastpay
package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var errorMessage by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    fun updateEmail(email: String) {
        loginEmail = email
    }

    fun updatePassword(password: String) {
        loginPassword = password
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                isLoading = true
                auth.signInWithEmailAndPassword(loginEmail, loginPassword).await()
                val user = auth.currentUser
                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (user?.isEmailVerified == true) {
                        errorMessage = ""
                        onSuccess()
                    } else {
                        auth.signOut()
                        errorMessage = "請先驗證您的電子郵件"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = e.message ?: "登入失敗"
                }
            }
        }
    }
}