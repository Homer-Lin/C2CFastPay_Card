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
import androidx.navigation.NavGraph.Companion.findStartDestination
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
        // 1. Home
        IconButton(onClick = {
            navController.navigate(Screen.Sale.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }) {
            Icon(painter = painterResource(R.drawable.img_2), contentDescription = "Home")
        }

        // 2. Add (修改處：改回導向 WishOrProductScreen)
        IconButton(onClick = {
            navController.navigate(Screen.WishOrProduct.route) { // <--- 改回這裡
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }) {
            Icon(painter = painterResource(R.drawable.img_3), contentDescription = "Add")
        }

        // 3. Connect
        IconButton(onClick = {
            navController.navigate(Screen.CardStack.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }) {
            Icon(painter = painterResource(R.drawable.img_4), contentDescription = "Connect")
        }

        // 4. Chat
        IconButton(onClick = {
            navController.navigate(Screen.History.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }) {
            Icon(painter = painterResource(R.drawable.img_5), contentDescription = "Chat")
        }

        // 5. User
        IconButton(onClick = {
            navController.navigate(Screen.User.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }) {
            Icon(painter = painterResource(R.drawable.img_6), contentDescription = "User")
        }
    }
}