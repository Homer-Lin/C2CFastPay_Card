package com.example.c2cfastpay_card.UIScreen.components


// 1. 確保 import ProductItem 和 UUID
import com.example.c2cfastpay_card.UIScreen.components.ProductItem
import java.util.UUID

/**
 * 用於儲存一次成功的「喜歡」(右滑) 紀錄
 * 我們儲存商品的關鍵資訊，以便在歷史紀錄中顯示
 */
data class MatchItem(
    val id: String = UUID.randomUUID().toString(), // 配對本身的 ID
    val productId: String, // 被喜歡的商品的 ID
    val productTitle: String, // 被喜歡的商品標題
    val productImageUrl: String, // 被喜歡的商品圖片網址
    val timestamp: Long = System.currentTimeMillis() // 配對發生的時間
)

/**
 * 一個輔助函數，方便從 ProductItem 轉換
 */
fun ProductItem.toMatchItem(): MatchItem {
    return MatchItem(
        productId = this.id, // 確保您的 ProductItem 有 id 欄位
        productTitle = this.title,
        productImageUrl = this.imageUri
    )
}