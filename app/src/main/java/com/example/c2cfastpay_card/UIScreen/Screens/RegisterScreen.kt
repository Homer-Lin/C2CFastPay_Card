package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.c2cfastpay_card.navigation.Screen
import com.example.c2cfastpay_card.ui.theme.C2CFastPay_CardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    onSwitchToLogin: () -> Unit
) {
    val viewModel: RegisterViewModel = viewModel()
    val primaryColor = Color(0xFF487F81)

    // ★★★ 修正重點：在這裡宣告狀態變數，解決 Unresolved reference ★★★
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("註冊帳號") },
                navigationIcon = {
                    IconButton(onClick = { onSwitchToLogin() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. 帳號輸入框
            OutlinedTextField(
                value = viewModel.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("帳號名稱 (顯示名稱)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = primaryColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = viewModel.errorMessage.contains("帳號")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 信箱
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("電子信箱") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = primaryColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                isError = viewModel.errorMessage.contains("信箱")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 密碼
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("設定密碼") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                isError = viewModel.errorMessage.contains("密碼")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 確認密碼
            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.updateConfirmPassword(it) },
                label = { Text("確認密碼") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                isError = viewModel.errorMessage.contains("不一致")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. 隱私條款
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = viewModel.privacyPolicyChecked,
                    onCheckedChange = { viewModel.updatePrivacyPolicyChecked(it) },
                    colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                )
                Text("我已閱讀並同意 ", fontSize = 14.sp)
                Text(
                    text = "隱私條款",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        // 點擊觸發彈窗
                        showPrivacyDialog = true
                    }
                )
            }

            // --- 隱私條款彈窗邏輯 ---
            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    containerColor = Color.White,
                    title = {
                        Text(text = "隱私權條款", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = """
                                    1. 隱私權保護政策
                                    本政策說明我們如何蒐集、使用、揭露、移轉與保護您的個人資料。

                                    2. 個人資料的蒐集與使用
                                    您註冊或使用服務時，我們可能蒐集姓名、Email、聯絡方式、使用紀錄等資料，並僅用於提供服務、帳號管理、客服、資訊通知及改善產品之目的。
                                    
                                    3. 資料保護
                                    我們採行合理技術與管理措施，以防止資料遭未經授權之存取、使用、修改或洩漏。
                                    
                                    4. 資料揭露與移轉
                                    除法律要求或為提供服務必要（如第三方服務供應商），我們不會向任何無關單位揭露您的資料，並確保接收方遵守相同保護標準。
                                    
                                    5. 資料保存期間
                                    資料僅在達成蒐集目的所需期間內保存，期滿後將刪除或匿名化。
                                    
                                    6. 您的權利
                                    您可依個資法請求查詢、更正、刪除、停止使用或撤回同意。我們將於法定期限內處理。
                                    
                                    7. 政策更新
                                    本政策可能因服務或法規調整而更新；更新後將立即生效並公布於網站或APP。
                                """.trimIndent(),
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showPrivacyDialog = false }
                        ) {
                            Text("我了解了", color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // 錯誤訊息顯示
            if (viewModel.errorMessage.isNotEmpty()) {
                Text(
                    text = viewModel.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. 註冊按鈕
            Button(
                onClick = {
                    viewModel.register(
                        onSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = viewModel.isRegisterEnabled && !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("立即註冊", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 重新發送驗證信按鈕
            if (viewModel.errorMessage.contains("驗證郵件已發送")) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.register(
                            onSuccess = {
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("重新發送驗證郵件")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    C2CFastPay_CardTheme {
        RegisterScreen(rememberNavController(), {})
    }
}