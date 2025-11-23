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
import com.example.c2cfastpay_card.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home 按鈕
        IconButton(onClick = { navController.navigate(Screen.Sale.route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }}) {
            Icon(
                painter = painterResource(R.drawable.img_2),
                contentDescription = "Home"
            )
        }

        // ==========================================
        // 【修改重點】Add 按鈕 -> 改去 Screen.AddStep1
        // ==========================================
        IconButton(onClick = {
            // 這裡改成 AddStep1.route，才會先去拍照頁面
            navController.navigate(Screen.AddStep1.route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }) {
            Icon(
                painter = painterResource(R.drawable.img_3),
                contentDescription = "Add"
            )
        }

        // Connect 按鈕
        IconButton(onClick = { navController.navigate(Screen.CardStack.route){
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }}) {
            Icon(
                painter = painterResource(R.drawable.img_4),
                contentDescription = "Connect"
            )
        }

        // Chat 按鈕
        IconButton(onClick = {
            // 如果您的 Screen.kt 有定義 Chat，可以把 TODO 改掉
            // navController.navigate(Screen.Chat.route)
        }) {
            Icon(
                painter = painterResource(R.drawable.img_5),
                contentDescription = "Chat"
            )
        }

        // User 按鈕
        IconButton(onClick = {
            // navController.navigate(Screen.User.route)
        }) {
            Icon(
                painter = painterResource(R.drawable.img_6),
                contentDescription = "User"
            )
        }
    }
}