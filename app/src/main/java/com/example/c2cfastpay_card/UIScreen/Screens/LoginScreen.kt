package com.example.c2cfastpay_card.UIScreen.Screens

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
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.c2cfastpay_card.navigation.Screen
import com.example.c2cfastpay_card.UIScreen.Screens.LoginViewModel
import com.example.c2cfastpay_card.ui.theme.C2CFastPay_CardTheme


@Composable
fun LoginScreen(
    navController: NavController,
    onSwitchToRegister: () -> Unit,
    onForgetPasswordClick: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel()
    val context = LocalContext.current

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
            selectedTabIndex = 1,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = false,
                onClick = onSwitchToRegister,
                text = { Text("註冊") }
            )
            Tab(
                selected = true,
                onClick = { /* 當前頁面，不需要動作 */ },
                text = { Text("登入") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = viewModel.loginEmail,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("信箱：", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = viewModel.errorMessage.isNotEmpty()
        )

        TextField(
            value = viewModel.loginPassword,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("密碼：", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

            isError = viewModel.errorMessage.isNotEmpty()
        )

        TextButton(
            onClick = onForgetPasswordClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("忘記密碼？", color = Color(0xFF007AFF), fontSize = 14.sp)
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

        Button(
            onClick = {
                viewModel.login {
                    // 【修改】使用 Screen.Sale.route
                    navController.navigate(Screen.Sale.route) {
                        // 【修改】使用 Screen.Login.route
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF),
                contentColor = Color.White
            ),
            enabled = viewModel.loginEmail.isNotBlank() && viewModel.loginPassword.isNotBlank() && !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("登入", fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // 【修正 3】 使用您專案的 Theme
    C2CFastPay_CardTheme {
        LoginScreen(
            navController = rememberNavController(),
            onSwitchToRegister = {},
            onForgetPasswordClick = {}
        )
    }
}