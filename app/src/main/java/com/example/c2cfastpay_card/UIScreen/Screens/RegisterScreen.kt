package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.c2cfastpay_card.ui.theme.C2CFastPay_CardTheme
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.c2cfastpay_card.navigation.Screen
import com.example.c2cfastpay_card.UIScreen.Screens.RegisterViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    onSwitchToLogin: () -> Unit
) {
    val viewModel: RegisterViewModel = viewModel()
    val context = LocalContext.current // 保留 context，目前未使用，但可能後續用於 Toast

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 返回按鈕
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Button"
            )
        }

        // 註冊/登入切換
        TabRow(
            selectedTabIndex = 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = true,
                onClick = { /* 當前頁面，不需要動作 */ },
                text = { Text("註冊") }
            )
            Tab(
                selected = false,
                onClick = onSwitchToLogin,
                text = { Text("登入") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = viewModel.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("信箱：", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = viewModel.errorMessage.contains("信箱")
        )

        TextField(
            value = viewModel.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("密碼：", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = viewModel.errorMessage.contains("密碼需")
        )

        TextField(
            value = viewModel.confirmPassword,
            onValueChange = { viewModel.updateConfirmPassword(it) },
            label = { Text("確認密碼：", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = viewModel.errorMessage.contains("不一致")
        )

        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = viewModel.privacyPolicyChecked,
                onCheckedChange = { viewModel.updatePrivacyPolicyChecked(it) }
            )
            Text("請先閱覽 ", fontSize = 14.sp)
            Text(
                text = "隱私條款",
                color = Color(0xFF007AFF),
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* 之後加隱私條款連結邏輯 */ }
            )
        }

        if (viewModel.errorMessage.isNotEmpty()) {
            Text(
                text = viewModel.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        Column {
            Button(
                onClick = {
                    viewModel.register(
                        onSuccess = {
                            // 【修改】使用 Screen.Login.route
                            navController.navigate(Screen.Login.route) {
                                // 【修改】使用 Screen.Register.route
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF),
                    contentColor = Color.White
                ),
                enabled = viewModel.isRegisterEnabled && !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("註冊", fontSize = 16.sp)
                }
            }

            // 當顯示驗證提示時，添加重新發送按鈕
            if (viewModel.errorMessage.contains("驗證郵件已發送")) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.register(
                            onSuccess = {
                                // 【修正】 使用 Screen 物件，而不是字串
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF),
                        contentColor = Color.White
                    ),
                    enabled = !viewModel.isLoading
                ) {
                    Text("重新發送驗證郵件", fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    // 【修正 3】 使用 C2CFastPay_CardTheme
    C2CFastPay_CardTheme {
        RegisterScreen(
            navController = rememberNavController(),
            onSwitchToLogin = {}
        )
    }
}