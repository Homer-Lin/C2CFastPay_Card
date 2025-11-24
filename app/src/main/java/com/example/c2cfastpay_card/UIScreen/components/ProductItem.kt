package com.example.c2cfastpay_card.UIScreen.components

import java.util.UUID

/**
 * 商品資料模型
 */
data class ProductItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val specs: String = "",
    val price: String = "",

    // 物流方式
    val payment: String = "",

    val notes: String = "",
    val other: String = "",

    // 單張圖片 (相容舊程式碼 / 列表顯示用)
    val imageUri: String = "",

    // ★★★【新增】多張圖片支援 ★★★
    val images: List<String> = emptyList(),

    // 擁有者資訊
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerEmail: String = "",

    val story: String = "",
    val stock: String = "1",
    val condition: String = "全新",

    val timestamp: Long = System.currentTimeMillis()
)