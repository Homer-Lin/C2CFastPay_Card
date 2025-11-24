package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    // 改名為 loginInput，因為它可能是 Email 也可能是帳號
    var loginInput by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var errorMessage by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    fun updateInput(input: String) {
        // 移除空白，避免誤觸
        loginInput = input.trim()
    }

    fun updatePassword(password: String) {
        loginPassword = password
    }

    fun login(onSuccess: () -> Unit) {
        if (loginInput.isBlank() || loginPassword.isBlank()) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            try {
                var emailToLogin = loginInput

                // 1. 判斷輸入的是否為 Email 格式
                val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(loginInput).matches()

                if (!isEmail) {
                    // 2. 如果不是 Email，假設是帳號，去 Firestore 查詢對應的 Email
                    val querySnapshot = db.collection("users")
                        .whereEqualTo("name", loginInput)
                        .limit(1)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        emailToLogin = querySnapshot.documents[0].getString("email") ?: ""
                    } else {
                        throw Exception("找不到此帳號")
                    }
                }

                // 3. 使用 (查到的或輸入的) Email 進行登入
                auth.signInWithEmailAndPassword(emailToLogin, loginPassword).await()
                val user = auth.currentUser

                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (user?.isEmailVerified == true) {
                        onSuccess()
                    } else {
                        auth.signOut()
                        errorMessage = "請先驗證您的電子郵件"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    // 優化錯誤訊息顯示
                    errorMessage = if (e.message?.contains("找不到此帳號") == true) {
                        "帳號不存在"
                    } else {
                        "登入失敗：帳號或密碼錯誤"
                    }
                }
            }
        }
    }
}