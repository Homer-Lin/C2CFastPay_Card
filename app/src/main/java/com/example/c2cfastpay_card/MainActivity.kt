package com.example.c2cfastpay_card

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.c2cfastpay_card.data.largeData
import com.example.c2cfastpay_card.navigation.AppNavigationGraph
import com.example.c2cfastpay_card.ui.theme.C2CFastPay_CardTheme

//import com.example.c2cfastpay_card.ui.SaleProductPage
//import com.example.c2cfastpay_card.ui.WishPreviewPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            C2CFastPay_CardTheme {
                val navController = rememberNavController()

                AppNavigationGraph(
                    navController = navController,
//                    cardData = largeData
                )
            }
        }
    }
}
