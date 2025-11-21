package com.example.c2cfastpay_card.UIScreen.components

import java.util.UUID

data class ProductItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val specs: String = "",
    val price: String = "",
    val payment: String = "",
    val notes: String = "",
    val other: String = "",
    val imageUri: String = "", // 這裡之後會存 "https://..." 的雲端網址

    // 【新增】擁有者資訊 (C2C 核心)
    val ownerId: String = "",       // 賣家的 Firebase UID
    val ownerName: String = "",     // 賣家的暱稱 (顯示在卡片上)
    val ownerEmail: String = "",    // 賣家的信箱

    val timestamp: Long = System.currentTimeMillis() // 上架時間 (用於排序)
)