package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c2cfastpay_card.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var username by mutableStateOf("")
    var privacyPolicyChecked by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var isRegisterEnabled by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    fun updateEmail(email: String) {
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

    fun updateUsername(username: String) {
        this.username = username.trim()
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
        val isUsernameValid = username.isNotBlank()

        isRegisterEnabled = isEmailValid && isPasswordValid && isConfirmPasswordValid && isUsernameValid && privacyPolicyChecked

        // 【修正】將這裡的英文改為中文
        errorMessage = when {
            !isUsernameValid && username.isNotEmpty() -> "請輸入帳號名稱"
            !isEmailValid && email.isNotEmpty() -> "請輸入有效的電子信箱"
            !isPasswordValid && password.isNotEmpty() -> "密碼長度至少需 6 個字元"
            !isConfirmPasswordValid && confirmPassword.isNotEmpty() -> "兩次輸入的密碼不一致"
            !privacyPolicyChecked -> "請勾選同意隱私權條款"
            else -> ""
        }
        // 如果所有欄位都是空的，不顯示錯誤訊息
        if(email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty() && username.isEmpty()) errorMessage = ""
    }

    fun register(onSuccess: () -> Unit) {
        if (isRegisterEnabled && !isLoading) {
            isLoading = true
            errorMessage = ""
            viewModelScope.launch {
                try {
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()

                    // 1. 建立 Auth 帳號
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val firebaseUser = result.user

                    if (firebaseUser != null) {
                        // 2. 建立 User 資料
                        val newUser = User(
                            id = firebaseUser.uid,
                            email = email,
                            name = username,
                            avatarUrl = ""
                        )

                        db.collection("users")
                            .document(firebaseUser.uid)
                            .set(newUser)
                            .await()

                        // 3. 發送驗證信
                        firebaseUser.sendEmailVerification().await()

                        withContext(Dispatchers.Main) {
                            isLoading = false
                            // 【修正】成功訊息也中文化
                            errorMessage = "驗證信已發送！若未收到，請檢查垃圾郵件匣，並點擊連結完成驗證。"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        // 【修正】錯誤訊息處理
                        // Firebase 的錯誤訊息通常是英文，我們可以簡單翻譯幾個常見的
                        errorMessage = when {
                            e.message?.contains("The email address is already in use") == true -> "此信箱已被註冊"
                            e.message?.contains("The email address is badly formatted") == true -> "信箱格式錯誤"
                            e.message?.contains("Password should be at least 6 characters") == true -> "密碼長度不足"
                            else -> "註冊失敗：${e.message}" // 其他錯誤保留原文以免誤判
                        }
                    }
                }
            }
        }
    }
}