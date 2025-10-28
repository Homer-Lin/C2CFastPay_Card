package com.example.c2cfastpay_card.navigation

/**
 * 定義所有畫面的「路由」(routes)。
 * 這是一個最佳實踐，可以避免在程式碼中直接使用字串，減少錯誤。
 */
sealed class Screen(val route: String) {
    // 卡片堆疊畫面
    object CardStack : Screen("card_stack_screen")

    // 卡片配對歷史紀錄畫面
    object History : Screen("history_screen")

    //二手物品販售頁面
    object Sale : Screen("sale_screen")

    object WishList : Screen("wish_list_screen")
    object AddProduct : Screen("add_product/{wishJson}") // 帶有參數的路由
    object AddWish : Screen("add_wish_screen")
    // 您未來可以從這裡擴充，例如：
    // object Profile : Screen("profile_screen")
}
