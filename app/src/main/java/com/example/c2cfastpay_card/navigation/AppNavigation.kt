package com.example.c2cfastpay_card.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.c2cfastpay_card.ProductFlowViewModel
import com.example.c2cfastpay_card.UIScreen.Screens.AIChatScreen
import com.example.c2cfastpay_card.UIScreen.Screens.AddProductScreen
import com.example.c2cfastpay_card.UIScreen.Screens.AddProductStepOne
import com.example.c2cfastpay_card.UIScreen.Screens.AddWishScreen
import com.example.c2cfastpay_card.UIScreen.Screens.CardStackScreen
import com.example.c2cfastpay_card.UIScreen.Screens.CartScreen
import com.example.c2cfastpay_card.UIScreen.Screens.ChatScreen
import com.example.c2cfastpay_card.UIScreen.Screens.ForgotPasswordScreen
import com.example.c2cfastpay_card.UIScreen.Screens.HistoryScreen
import com.example.c2cfastpay_card.UIScreen.Screens.LoginScreen
import com.example.c2cfastpay_card.UIScreen.Screens.MyProductsScreen
import com.example.c2cfastpay_card.UIScreen.Screens.ProductDetailScreen
import com.example.c2cfastpay_card.UIScreen.Screens.RegisterScreen
import com.example.c2cfastpay_card.UIScreen.Screens.SaleProductPage
import com.example.c2cfastpay_card.UIScreen.Screens.UserScreen
import com.example.c2cfastpay_card.UIScreen.Screens.WishOrProductScreen
import com.example.c2cfastpay_card.UIScreen.Screens.WishPreviewPage
import com.example.c2cfastpay_card.UIScreen.Screens.WishDetailScreen
import com.example.c2cfastpay_card.UIScreen.components.WishRepository
import com.google.gson.Gson

/**
 * 這是「導航圖」(NavHost)。
 * 它就像一個「總控制器」，根據 NavController 的指令，決定現在該顯示哪個畫面。
 */
@Composable
fun AppNavigationGraph(
    navController: NavHostController,
) {
    // 【重要】宣告 ProductFlowViewModel (共用 ViewModel)
    // 這樣 AddStep1 可以存照片，AIChatScreen 可以讀取照片
    val productFlowViewModel: ProductFlowViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route // 設定 App 啟動時的第一個畫面
    ) {

        // --- 登入/註冊流程 ---
        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onSwitchToRegister = { navController.navigate(Screen.Register.route) },
                onForgetPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                onSwitchToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                onConfirmSuccess = { navController.popBackStack() },
                onSwitchToLogin = {
                    navController.popBackStack() // 返回上一頁 (即登入頁)
                }
            )
        }

        // --- 主功能頁面 ---

        // 1. 卡片堆疊
        composable(route = Screen.CardStack.route) {
            CardStackScreen(navController = navController)
        }

        // 2. 歷史紀錄
        composable(route = Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        // 3. 販售首頁
        composable(route = Screen.Sale.route) {
            SaleProductPage(navController = navController)
        }

        // 4. 許願池
        composable(route = Screen.WishList.route) {
            WishPreviewPage(navController = navController)
        }

        composable(
            route = "add_product?draftJson={draftJson}&wishUuid={wishUuid}",
            // ... arguments 設定 ...
        ) { backStackEntry ->
            val context = LocalContext.current
            val wishRepository = remember { WishRepository(context) }

            // 取得參數
            val draftJson = backStackEntry.arguments?.getString("draftJson")
            val wishUuid = backStackEntry.arguments?.getString("wishUuid")

            var finalJsonForScreen by remember { mutableStateOf<String?>(draftJson) }

            // ★ 關鍵：如果有 wishUuid，就去抓資料並轉成 JSON
            LaunchedEffect(wishUuid) {
                if (wishUuid != null && wishUuid != "null" && wishUuid.isNotEmpty()) {
                    val wishItem = wishRepository.getWishByUuid(wishUuid)
                    if (wishItem != null) {
                        // 轉成 JSON 字串傳給 Screen
                        finalJsonForScreen = Gson().toJson(wishItem)
                    }
                }
            }

            // 顯示 Screen
            AddProductScreen(
                navController = navController,
                draftJson = finalJsonForScreen // 這裡會把 Wish 的資料傳進去
            )
        }

        // 5. 新增選擇頁 (許願或商品)
        composable(route = Screen.WishOrProduct.route) {
            WishOrProductScreen(navController = navController)
        }

        // ==========================================
        // 【流程】上架第一步：拍照/選圖 (AddStep1)
        // ==========================================
        composable(route = Screen.AddStep1.route) {
            AddProductStepOne(
                navController = navController,
                flowViewModel = productFlowViewModel
            )
        }

        // ==========================================
        // 【流程】AI 上架助手 (AIChat)
        // ==========================================
        composable(
            route = "ai_chat?imageUri={imageUri}",
            arguments = listOf(navArgument("imageUri") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AIChatScreen(
                navController = navController,
                flowViewModel = productFlowViewModel
            )
        }

        // ==========================================
        // 【流程】上架填寫頁 (AddProduct)
        // 同時支援：1. AI/手動帶入的草稿 (draftJson)  2. 許願池帶入的資料 (wishUuid)
        // ==========================================
        composable(
            route = "add_product?draftJson={draftJson}&wishUuid={wishUuid}",
            arguments = listOf(
                navArgument("draftJson") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("wishUuid") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val wishRepository = remember { WishRepository(context) }

            // 取得參數
            val draftJson = backStackEntry.arguments?.getString("draftJson")
            val wishUuid = backStackEntry.arguments?.getString("wishUuid")

            // 用來決定最後要傳給 Screen 的 JSON
            var finalJsonForScreen by remember { mutableStateOf<String?>(draftJson) }
            var isLoading by remember { mutableStateOf(false) }

            // 如果有 wishUuid，代表是從許願池來的，需要去資料庫抓資料
            LaunchedEffect(wishUuid) {
                if (wishUuid != null && wishUuid != "null" && wishUuid.isNotEmpty()) {
                    isLoading = true
                    val wishItem = wishRepository.getWishByUuid(wishUuid)
                    Log.d("NavGraph", "從許願池抓取資料: $wishItem")

                    if (wishItem != null) {
                        finalJsonForScreen = Gson().toJson(wishItem)
                    }
                    isLoading = false
                }
            }

            // 顯示畫面
            if (!isLoading) {
                AddProductScreen(
                    navController = navController,
                    draftJson = finalJsonForScreen
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // --- 其他功能 ---

        composable(route = Screen.AddWish.route) {
            AddWishScreen(navController = navController)
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                navController = navController,
                productId = productId
            )
        }

        composable(route = Screen.Cart.route) {
            CartScreen(navController = navController)
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("matchId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            ChatScreen(
                navController = navController,
                matchId = matchId
            )
        }

        // ==========================================
        // 【新增】會員相關頁面 (補上遺漏的部分)
        // ==========================================
        composable(route = Screen.User.route) {
            UserScreen(navController = navController)
        }

        composable(route = Screen.MyProducts.route) {
            MyProductsScreen(navController = navController)
        }

        composable(
            route = "wish_detail/{wishUuid}",
            arguments = listOf(navArgument("wishUuid") { type = NavType.StringType })
        ) { backStackEntry ->
            val wishUuid = backStackEntry.arguments?.getString("wishUuid") ?: ""
            WishDetailScreen(navController = navController, wishUuid = wishUuid)
        }

    }
}