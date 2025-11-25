package com.example.c2cfastpay_card.UIScreen.Screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Notifications // 【正確引入 Notifications】
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.c2cfastpay_card.UIScreen.components.BottomNavigationBar
import com.example.c2cfastpay_card.navigation.Screen

@Composable
fun UserScreen(
    navController: NavController
) {
    val viewModel: UserViewModel = viewModel()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // 監聽 ViewModel 的 Toast 事件
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            if (message.isNotBlank()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(it) }
    }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    val primaryColor = Color(0xFF487F81)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
            ) {
                // --- 頂部個人資料區 ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            color = primaryColor,
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // 大頭貼區塊
                        Box(modifier = Modifier.size(100.dp)) {
                            // 預設頭像邏輯
                            if (!user?.avatarUrl.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(user!!.avatarUrl),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { imagePicker.launch("image/*") },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // 顯示灰色預設人像
                                Image(
                                    painter = rememberVectorPainter(Icons.Default.Person),
                                    contentDescription = "Default Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White) // 白底
                                        .padding(16.dp) // 內縮一點
                                        .clickable { imagePicker.launch("image/*") },
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(Color.LightGray) // 灰色圖示
                                )
                            }

                            // 編輯圖示
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Avatar",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .background(Color.Black.copy(0.5f), CircleShape)
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                newNameInput = user?.name ?: ""
                                showEditNameDialog = true
                            }
                        ) {
                            Text(
                                text = user?.name ?: "載入中...",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(0.8f), modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = user?.email ?: "",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- 選單列表 ---
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                    // 連接到「我的商品」
                    MenuCard(
                        icon = Icons.Default.ShoppingBag,
                        title = "我的商品管理",
                        subtitle = "查看或下架您上架的商品",
                        onClick = {
                            navController.navigate(Screen.MyProducts.route)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuCard(
                        icon = Icons.Default.Notifications, // 現在這裡不會報錯了
                        title = "通知中心",
                        subtitle = "查看配對成功與系統訊息",
                        onClick = {
                            Toast.makeText(context, "功能開發中", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuCard(
                        icon = Icons.Default.LockReset,
                        title = "修改密碼",
                        subtitle = "發送重設密碼信件至信箱",
                        onClick = {
                            viewModel.sendResetPasswordEmail()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuCard(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "登出帳號",
                        textColor = Color.Red,
                        onClick = {
                            viewModel.logout {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }

        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text("修改暱稱") },
                text = {
                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        label = { Text("新暱稱") }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateName(newNameInput)
                            showEditNameDialog = false
                        }
                    ) { Text("儲存") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) { Text("取消") }
                }
            )
        }
    }
}

@Composable
fun MenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (textColor == Color.Red) Color.Red else Color(0xFF487F81),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}