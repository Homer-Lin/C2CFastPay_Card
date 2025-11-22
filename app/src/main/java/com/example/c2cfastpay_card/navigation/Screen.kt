package com.example.c2cfastpay_card.navigation

/**
 * 定義所有畫面的「路由」(routes)。
 * 這是一個最佳實踐，可以避免在程式碼中直接使用字串，減少錯誤。
 */
sealed class Screen(val route: String) {

    object CardStack : Screen("card_stack_screen")
    object History : Screen("history_screen")
    object Sale : Screen("sale_screen")
    object WishList : Screen("wish_list_screen")

    object AddProduct : Screen("add_product?wishUuid={wishUuid}")
    object AddWish : Screen("add_wish_screen")

    // 您未來可以從這裡擴充，例如：
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")

    object ProductDetail : Screen("product_detail/{productId}")
    object Cart : Screen("cart_screen")
    // object Profile : Screen("profile_screen")
}
