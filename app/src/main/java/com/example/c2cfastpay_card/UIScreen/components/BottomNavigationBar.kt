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
import androidx.navigation.NavGraph.Companion.findStartDestination

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
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }}) {
            Icon(
                painter = painterResource(R.drawable.img_2),
                contentDescription = "Home"
            )
        }

        // Add 按鈕
        IconButton(onClick = {
            // ★ 修改這裡：連到選擇頁面
            navController.navigate(Screen.WishOrProduct.route) {
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
            // 【修改】導航到「聊天列表 (History)」
            navController.navigate(Screen.History.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }) {
            Icon(
                painter = painterResource(R.drawable.img_5),
                contentDescription = "Chat"
            )
        }

        // User 按鈕
        IconButton(onClick = {
            // 【修改】導航到「會員中心 (User)」
            navController.navigate(Screen.User.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }) {
            Icon(
                painter = painterResource(R.drawable.img_6),
                contentDescription = "User"
            )
        }
    }
}