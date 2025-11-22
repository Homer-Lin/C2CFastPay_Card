package com.example.c2cfastpay_card.data

import java.util.UUID

/**
 * 購物車項目資料模型
 */
data class CartItem(
    val id: String = UUID.randomUUID().toString(), // 購物車項目的唯一 ID

    // 商品資訊 (建立快照，方便顯示)
    val productId: String = "",
    val productTitle: String = "",
    val productPrice: String = "",
    val productImage: String = "",

    // 賣家資訊 (方便結帳時知道是跟誰買的)
    val sellerId: String = "",

    // 購物資訊
    val quantity: Int = 1,           // 購買數量
    val addedAt: Long = System.currentTimeMillis() // 加入時間
)