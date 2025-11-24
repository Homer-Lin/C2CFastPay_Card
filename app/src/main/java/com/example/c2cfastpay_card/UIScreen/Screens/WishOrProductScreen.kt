package com.example.c2cfastpay_card.UIScreen.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.c2cfastpay_card.UIScreen.components.BottomNavigationBar
import com.example.c2cfastpay_card.navigation.Screen

@Composable
fun WishOrProductScreen(navController: NavController) {
    Scaffold(
        // 【關鍵】加入 BottomNavigationBar
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("請選擇新增項目", fontSize = 24.sp, color = Color.Black, style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(40.dp))

            // 上架商品按鈕
            Button(
                onClick = { navController.navigate(Screen.AddProduct.route) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF487F81)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("上架商品 (Sell)", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 許願按鈕
            Button(
                onClick = { navController.navigate(Screen.AddWish.route) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("新增許願 (Wish)", fontSize = 20.sp)
            }
        }
    }
}