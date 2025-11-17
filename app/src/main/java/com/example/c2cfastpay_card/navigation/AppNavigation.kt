package com.example.c2cfastpay_card.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.c2cfastpay_card.UIScreen.Screens.HistoryScreen
import com.example.c2cfastpay_card.UIScreen.Screens.SaleProductPage
import com.example.c2cfastpay_card.UIScreen.Screens.WishPreviewPage
import com.example.c2cfastpay_card.UIScreen.Screens.AddProductScreen
import com.example.c2cfastpay_card.UIScreen.Screens.AddWishScreen
import com.example.c2cfastpay_card.UIScreen.Screens.CardStackScreen
import com.example.c2cfastpay_card.data.CardData
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import com.google.gson.Gson

import com.example.c2cfastpay_card.UIScreen.Screens.LoginScreen
import com.example.c2cfastpay_card.UIScreen.Screens.RegisterScreen
import com.example.c2cfastpay_card.UIScreen.Screens.ForgotPasswordScreen
/**
 * 這是「導航圖」(NavHost)。
 * 它就像一個「總控制器」，根據 NavController 的指令，決定現在該顯示哪個畫面。
 */
@Composable
fun AppNavigationGraph(
    navController: NavHostController,
//    cardData: List<CardData> // 我們需要將卡片資料傳遞給 CardStack
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route // 設定 App 啟動時的第一個畫面
    ) {

        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onSwitchToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onForgetPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                onSwitchToLogin = {
                    navController.popBackStack() // 返回登入頁
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                onConfirmSuccess = {
                    navController.popBackStack() // 返回登入頁
                }
            )
        }
        // --- 卡片堆疊 ---
        composable(route = Screen.CardStack.route) {
            CardStackScreen(navController = navController)
        }
        // --- 歷史紀錄 ---
        composable(route = Screen.History.route) {
            HistoryScreen(navController = navController) // 將「遙控器」傳遞下去
        }

        composable(route = Screen.Sale.route) {
            SaleProductPage(navController = navController)
        }

        composable(route = Screen.WishList.route) {
            WishPreviewPage(navController = navController)
        }

// --- 【步驟二：完整替換 'AddProduct' 區塊】 ---
        // 4. 快速上架畫面 (修改為接收 wishUuid)
        composable(
            route = Screen.AddProduct.route, // 使用 Screen.kt 的新路徑 "add_product?wishUuid={wishUuid}"
            arguments = listOf(navArgument("wishUuid") { // 1. 改為接收 wishUuid
                type = NavType.StringType
                nullable = true       // 參數是可選的
                defaultValue = null // 預設值為 null
            })
        ) { backStackEntry ->

            val context = LocalContext.current
            val wishRepository = remember { WishRepository(context) }
            val wishUuid = backStackEntry.arguments?.getString("wishUuid")
            var wishJsonForScreen by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(wishUuid) {
                if (wishUuid != null) {
                    // 【這就是您說的「抓資料」的動作】
                    // 呼叫我們在步驟三新增的函式

                    val wishItem = wishRepository.getWishByUuid(wishUuid)
                    Log.d("DataFlowDebug", "步驟 3: 從 Repository 抓到 WishItem = $wishItem")
                    if (wishItem != null) {
                        wishJsonForScreen = Gson().toJson(wishItem) // 轉換為 JSON
                        Log.d("DataFlowDebug", "步驟 4: 轉換為 JSON = $wishJsonForScreen")
                    }
                }
                isLoading = false // 標記加載完成
            }

            // 只有在加載完成後 (isLoading = false) 才顯示 AddProductScreen
            if (!isLoading) {
                Log.d("DataFlowDebug", "步驟 5: 傳遞 JSON 給 AddProductScreen = $wishJsonForScreen")
                // 【安全地傳遞資料】
                // 這裡會安全地傳入 wishJson (可能為 null)
                // 您的 AddProductScreen 檔案 不需要修改
                // 它內部的邏輯 會完美處理這種情況
                AddProductScreen(
                    navController = navController,
                    wishJson = wishJsonForScreen
                )
            }
        }
        // --- 【修改區塊結束】 ---

        composable(route = Screen.AddWish.route) {
            AddWishScreen(navController = navController)
        }
    }
}