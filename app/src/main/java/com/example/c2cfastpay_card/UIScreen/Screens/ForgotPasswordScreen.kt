package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
// 【修改】 M3 的 ArrowBack 在 automirrored.filled
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// 【修改】 修正 import 路徑，匯入 C2CFastPay_CardTheme
import com.example.c2cfastpay_card.ui.theme.C2CFastPay_CardTheme
import androidx.compose.foundation.background
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    onConfirmSuccess: () -> Unit,
    onSwitchToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 返回按鈕
        IconButton(onClick = {
            // 【修改】 呼叫 onSwitchToLogin (或 popBackStack)
            // 既然您在函式定義中加入了 onSwitchToLogin，我們就使用它
            onSwitchToLogin()
        }) {
            Icon(
                // 【修改】 M3 的 ArrowBack 路徑
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Button"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("信箱：", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),

            // --- 【修改：移除整個 colors 區塊】 ---
            // 由於 M3 版本過舊，
            // 導致 API 不相容。
            // 移除後，TextField 會使用 C2CFastPay_CardTheme 的預設顏色。
            // colors = TextFieldDefaults.colors(
            //    ... (移除這整個區塊) ...
            // ),
            // --- 【修改結束】 ---

            isError = errorMessage.isNotEmpty()
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    isLoading = true
                    errorMessage = ""
                    // 【修改】 建議使用 viewModelScope，但 CoroutineScope 也能運作
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                onConfirmSuccess()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                errorMessage = e.message ?: "重設密碼失敗"
                            }
                        }
                    }
                } else {
                    errorMessage = "請輸入有效的信箱"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF),
                contentColor = Color.White
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("重設密碼", fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    // 【修改】 使用您正確的 Theme 名稱
    C2CFastPay_CardTheme {
        ForgotPasswordScreen(
            navController = rememberNavController(),
            onConfirmSuccess = {}
            // onSwitchToLogin 由於有預設值，Preview 中可以不傳
        )
    }
}