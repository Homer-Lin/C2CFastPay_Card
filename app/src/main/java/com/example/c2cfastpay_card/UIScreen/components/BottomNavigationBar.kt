package com.example.c2cfastpay_card.UIScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.c2cfastpay_card.R
import com.example.c2cfastpay_card.navigation.Screen // 匯入您的路由

@Composable
fun BottomNavigationBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White), // 您可以自訂背景色
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically // 確保圖示垂直置中
    ) {
        // Home 按鈕 (導航到 Sale 畫面)
        IconButton(onClick = { navController.navigate(Screen.Sale.route) {
            // 可選：避免在返回堆疊中累積大量相同的畫面
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }}) {
            Icon(
                painter = painterResource(R.drawable.img_2), // Home 圖示
                contentDescription = "Home"
            )
        }

        // Add 按鈕
        IconButton(onClick = {navController.navigate(Screen.AddProduct.route){
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }}) {
            Icon(
                painter = painterResource(R.drawable.img_3), // Add 圖示
                contentDescription = "Add"
            )
        }

        // Connect 按鈕 (導航到 CardStack 畫面)
        IconButton(onClick = { navController.navigate(Screen.CardStack.route){
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }}) {
            Icon(
                painter = painterResource(R.drawable.img_4), // Connect 圖示
                contentDescription = "Connect"
            )
        }

        // Chat 按鈕
        IconButton(onClick = { /* TODO: 導航到 Chat */ }) {
            Icon(
                painter = painterResource(R.drawable.img_5), // Chat 圖示
                contentDescription = "Chat"
            )
        }

        // User 按鈕
        IconButton(onClick = { /* TODO: 導航到 User */ }) {
            Icon(
                painter = painterResource(R.drawable.img_6), // User 圖示
                contentDescription = "User"
            )
        }
    }
}