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

class RegisterViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var privacyPolicyChecked by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var isRegisterEnabled by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    fun updateEmail(email: String) {
        this.email = email
        checkRegistrationValidity()
    }

    fun updatePassword(password: String) {
        this.password = password
        checkRegistrationValidity()
    }

    fun updateConfirmPassword(confirmPassword: String) {
        this.confirmPassword = confirmPassword
        checkRegistrationValidity()
    }

    fun updatePrivacyPolicyChecked(checked: Boolean) {
        privacyPolicyChecked = checked
        checkRegistrationValidity()
    }

    private fun checkRegistrationValidity() {
        val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        val isPasswordValid = password.length >= 6
        val isConfirmPasswordValid = password == confirmPassword
        isRegisterEnabled = isEmailValid && isPasswordValid && isConfirmPasswordValid && privacyPolicyChecked
        errorMessage = when {
            !isEmailValid && email.isNotEmpty() -> "請輸入有效的信箱"
            !isPasswordValid && password.isNotEmpty() -> "密碼需至少 6 個字元"
            !isConfirmPasswordValid && confirmPassword.isNotEmpty() -> "密碼不一致"
            !privacyPolicyChecked -> "請勾選隱私條款" // 這裡只是邏輯判斷，UI 顯示可能不同
            else -> ""
        }
        // 如果全空則不顯示錯誤
        if(email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty()) errorMessage = ""
    }

    fun register(onSuccess: () -> Unit) {
        if (isRegisterEnabled && !isLoading) {
            isLoading = true
            errorMessage = ""
            viewModelScope.launch {
                try {
                    val auth = FirebaseAuth.getInstance()
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    // 發送驗證郵件
                    result.user?.sendEmailVerification()?.await()
                    val user = auth.currentUser
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        if (user?.isEmailVerified == true) {
                            onSuccess()
                        } else {
                            errorMessage = "驗證郵件已發送，請檢查信箱並點擊連結完成驗證"
                            // 不直接調用 onSuccess，等待用戶驗證
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        errorMessage = e.message ?: "註冊失敗"
                    }
                }
            }
        }
    }
}