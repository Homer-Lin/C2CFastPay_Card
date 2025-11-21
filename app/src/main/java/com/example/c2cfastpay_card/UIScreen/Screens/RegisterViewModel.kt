package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.data.User // 【新增】導入 User 模型
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // 【新增】導入 Firestore
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
        // 使用 trim() 移除前後空白，避免使用者誤觸空白鍵導致驗證失敗
        this.email = email.trim()
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
        val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.length >= 6
        val isConfirmPasswordValid = password == confirmPassword
        isRegisterEnabled = isEmailValid && isPasswordValid && isConfirmPasswordValid && privacyPolicyChecked
        errorMessage = when {
            !isEmailValid && email.isNotEmpty() -> "請輸入有效的信箱"
            !isPasswordValid && password.isNotEmpty() -> "密碼需至少 6 個字元"
            !isConfirmPasswordValid && confirmPassword.isNotEmpty() -> "密碼不一致"
            !privacyPolicyChecked -> "請勾選隱私條款"
            else -> ""
        }
        if(email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty()) errorMessage = ""
    }

    fun register(onSuccess: () -> Unit) {
        if (isRegisterEnabled && !isLoading) {
            isLoading = true
            errorMessage = ""
            viewModelScope.launch {
                try {
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance() // 【新增】取得 Firestore 實例

                    // 1. 建立 Auth 帳號
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val firebaseUser = result.user

                    if (firebaseUser != null) {
                        // 2. 【關鍵修改】建立 User 資料並寫入 Firestore
                        val newUser = User(
                            id = firebaseUser.uid,
                            email = email,
                            // 暫時用 email @ 前面的字當預設暱稱
                            name = email.substringBefore("@"),
                            avatarUrl = "" // 暫時留空
                        )

                        // 寫入 "users" 集合，文件 ID 就是用戶的 UID
                        db.collection("users")
                            .document(firebaseUser.uid)
                            .set(newUser)
                            .await()

                        // 3. 發送驗證郵件
                        firebaseUser.sendEmailVerification().await()

                        withContext(Dispatchers.Main) {
                            isLoading = false
                            // 這裡邏輯不變：雖然寫入資料庫了，但還是要等他驗證信箱
                            errorMessage = "驗證郵件已發送，請檢查信箱並點擊連結完成驗證"
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