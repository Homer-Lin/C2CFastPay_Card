package com.example.c2cfastpay_card.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.c2cfastpay_card.UIScreen.Screens.CardStack
import com.example.c2cfastpay_card.UIScreen.Screens.HistoryScreen
import com.example.c2cfastpay_card.UIScreen.Screens.SaleProductPage
import com.example.c2cfastpay_card.UIScreen.Screens.WishPreviewPage
import com.example.c2cfastpay_card.UIScreen.Screens.AddProductScreen
import com.example.c2cfastpay_card.UIScreen.Screens.AddWishScreen
import com.example.c2cfastpay_card.data.CardData

/**
 * 這是「導航圖」(NavHost)。
 * 它就像一個「總控制器」，根據 NavController 的指令，決定現在該顯示哪個畫面。
 */
@Composable
fun AppNavigationGraph(
    navController: NavHostController,
    cardData: List<CardData> // 我們需要將卡片資料傳遞給 CardStack
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Sale.route // 設定 App 啟動時的第一個畫面
    ) {
        // --- 卡片堆疊 ---
        composable(route = Screen.CardStack.route) {
            CardStack(
                data = cardData,
                navController = navController // 將「遙控器」傳遞下去
            )
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

        // 4. 快速上架畫面 (帶有參數)
        composable(
            route = Screen.AddProduct.route, // 使用更新後的 route "add_product?wishJson={wishJson}"
            arguments = listOf(navArgument("wishJson") {
                type = NavType.StringType
                nullable = true       // <--- 將參數設為可選
                defaultValue = null // <--- 提供預設值 null
            })
        ) { backStackEntry ->
            // 現在如果 wishJson 參數不存在，getString 會回傳 null
            val wishJson = backStackEntry.arguments?.getString("wishJson")
            // 傳遞可能為 null 的 wishJson 給 AddProductScreen
            AddProductScreen(navController = navController, wishJson = wishJson)
        }

        composable(route = Screen.AddWish.route) {
            AddWishScreen(navController = navController)
            // --- 您可以在此加入更多 composable() 來定義更多畫面 ---
        }
    }
}
