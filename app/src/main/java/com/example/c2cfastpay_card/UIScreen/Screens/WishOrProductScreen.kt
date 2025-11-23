package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.c2cfastpay_card.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishOrProductScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, // 不需要標題
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 垂直置中
        ) {

            // 按鈕 1：我要上架 (綠色)
            BigSelectionButton(
                text = "我要上架",
                color = Color(0xFF487F81),
                onClick = {
                    // ★ 建議：連到 AddStep1 (拍照/AI流程)
                    // 如果你想直接手動填寫不經過AI，請改成 Screen.AddProduct.route
                    navController.navigate(Screen.AddStep1.route)
                }
            )

            Spacer(modifier = Modifier.height(32.dp)) // 兩個按鈕中間的距離

            // 按鈕 2：我要許願 (橘色)
            BigSelectionButton(
                text = "我要許願",
                color = Color(0xFFFF9800),
                onClick = {
                    navController.navigate(Screen.AddWish.route)
                }
            )
        }
    }
}

// 抽取出來的共用按鈕樣式 (正方形、圓角)
@Composable
fun BigSelectionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(160.dp) // 設定正方形大小 (可自行調整)
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp), // 圓角
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}